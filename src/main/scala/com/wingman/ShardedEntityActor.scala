package com.wingman

import akka.actor.{Actor, ActorLogging, PoisonPill, Timers}
import redis.clients.jedis.Jedis
import scala.concurrent.duration._

case class GenericMapRequest(fields: Map[String, String])
case class SaveState(shardKey: String, shardValue: String, state: String)
case class RetrieveState(shardKey: String, shardValue: String)
//case object SaveSnapshot
case object StopActor

class ShardedEntityActor extends Actor with ActorLogging with Timers {
  private var state: String = ""
  private var shardKey: Option[String] = None
  private var shardValue: Option[String] = None

  // Redis key structure: shard:<shardKey>:<shardValue>
  private def redisKey: String =
    shardKey.zip(shardValue).map { case (key, value) => s"$key:$value" }.getOrElse("")

  // Schedule periodic snapshot saving every minute
  timers.startTimerWithFixedDelay("SaveSnapshotTimer", SaveSnapshot, 1.minute)

  override def preStart(): Unit = {
    log.info(s"Actor [${self.path.name}] starting...")
  }

  override def postStop(): Unit = {
    // Persist state to Redis before shutdown
    if (shardKey.isDefined && shardValue.isDefined) {
      RedisClient.saveState(redisKey, state)
      log.info(s"Persisted final state for [$redisKey]: $state")
    }
  }

  override def receive: Receive = {
    case GenericMapRequest(fields) =>
      shardKey = fields.get("shardKey")
      shardValue = fields.get("shardValue")
      if (shardKey.isEmpty || shardValue.isEmpty) {
        log.warning("Invalid request: shardKey or shardValue missing.")
        sender() ! "Error: shardKey and shardValue are required."
      } else {
        // Load state from Redis if not already loaded
        loadStateIfNecessary()
        val operation = fields.getOrElse("operation", "unknown").toLowerCase
        operation match {
          case "update" =>
            val newState = fields.getOrElse("value", "")
            updateState(newState)
            sender() ! s"State updated for [$redisKey]: $state"

          case "reset" =>
            resetState()
            sender() ! s"State reset for [$redisKey]."

          case "get" =>
            sender() ! s"Current state for [$redisKey]: $state"

          case _ =>
            sender() ! s"Unknown operation [$operation] for [$redisKey]."
        }
      }

    case SaveSnapshot =>
      saveSnapshot()
      log.info(s"Snapshot saved for [$redisKey].")

    case RetrieveState(shardKeyReq, shardValueReq) =>
      if (shardKey.contains(shardKeyReq) && shardValue.contains(shardValueReq)) {
        sender() ! s"State for [$redisKey]: $state"
      } else {
        sender() ! s"Error: Requested state for [$shardKeyReq:$shardValueReq] not found."
      }

    case SaveState(shardKeyReq, shardValueReq, newState) =>
      if (shardKey.contains(shardKeyReq) && shardValue.contains(shardValueReq)) {
        updateState(newState)
        sender() ! s"State saved for [$redisKey]: $state"
      } else {
        sender() ! s"Error: Mismatched shardKey/shardValue [$shardKeyReq:$shardValueReq]."
      }

    case StopActor =>
      context.stop(self)

    case other =>
      log.warning(s"Unhandled message: $other")
  }

  private def loadStateIfNecessary(): Unit = {
    if (shardKey.isDefined && shardValue.isDefined && state.isEmpty) {
      RedisClient.getState(redisKey) match {
        case Some(savedState) =>
          state = savedState
          log.info(s"Restored state for [$redisKey]: $state")
        case None =>
          log.info(s"No state found for [$redisKey], initializing with empty state.")
      }
    }
  }

  private def updateState(newState: String): Unit = {
    state = state + newState // Appending for simplicity; customize as needed
    RedisClient.saveState(redisKey, state)
  }

  private def resetState(): Unit = {
    state = ""
    RedisClient.saveState(redisKey, state)
  }

  private def saveSnapshot(): Unit = {
    RedisClient.saveState(s"$redisKey:snapshot", state)
  }
}

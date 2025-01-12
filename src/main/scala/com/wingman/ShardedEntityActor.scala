package com.wingman

import akka.actor.{Actor, ActorLogging, Timers}
import scala.concurrent.duration._

case class GenericMapRequest(fields: Map[String, String])
//case object SaveSnapshot

class ShardedEntityActor extends Actor with ActorLogging with Timers {
  private var state: String = ""
  private var currentShardKey: Option[String] = None
  private var currentShardValue: Option[String] = None

  // Redis key structure: shardKey:shardValue
  private def redisKey: String = {
    (currentShardKey, currentShardValue) match {
      case (Some(key), Some(value)) => s"$key:$value"
      case _ => ""
    }
  }

  // Schedule periodic snapshot saving every minute
  timers.startTimerWithFixedDelay("SaveSnapshotTimer", SaveSnapshot, 1.minute)

  override def preStart(): Unit = {
    log.info(s"Actor [${self.path.name}] starting...")
  }

  override def postStop(): Unit = {
    saveStateToRedis()
    log.info(s"Persisted final state for [$redisKey]: $state before stopping.")
  }

  override def receive: Receive = {
    case GenericMapRequest(fields) =>
      val shardKey = fields.get("shardKey")
      val shardValue = fields.get("shardValue")

      if (shardKey.isEmpty || shardValue.isEmpty) {
        log.warning("Invalid request: shardKey or shardValue missing.")
        sender() ! "Error: shardKey and shardValue are required."
      } else {
        switchContext(shardKey.get, shardValue.get) // Ensure state isolation
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
      saveStateToRedis()
      log.info(s"Snapshot saved for [$redisKey].")
  }

  /**
   * Switches the actor context to a new shardKey and shardValue, loading state from Redis if necessary.
   */
  private def switchContext(shardKey: String, shardValue: String): Unit = {
    if (currentShardKey != Some(shardKey) || currentShardValue != Some(shardValue)) {
      saveStateToRedis() // Save current state before switching context
      currentShardKey = Some(shardKey)
      currentShardValue = Some(shardValue)
      loadStateFromRedis() // Load new state
    }
  }

  /**
   * Saves the current state to Redis.
   */
  private def saveStateToRedis(): Unit = {
    if (redisKey.nonEmpty) {
      RedisClient.saveState(redisKey, state)
      log.info(s"Persisted state for [$redisKey]: $state")
    }
  }

  /**
   * Loads the current state from Redis.
   */
  private def loadStateFromRedis(): Unit = {
    val redisState = RedisClient.getState(redisKey)
    redisState match {
      case Some(value) =>
        state = value
        log.info(s"Restored state for [$redisKey]: $state")
      case None =>
        log.info(s"No state found for [$redisKey], initializing with empty state.")
        state = "" // This happens only if the key is not found in Redis.
    }
  }

  /**
   * Updates the actor state and saves it to Redis.
   */
  private def updateState(newState: String): Unit = {
    state = newState // Replace existing state (change logic if appending is required)
    saveStateToRedis()
  }

  /**
   * Resets the actor state to an empty string and saves it to Redis.
   */
  private def resetState(): Unit = {
    state = ""
    saveStateToRedis()
  }
}

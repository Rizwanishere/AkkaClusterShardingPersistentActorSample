package com.wingman

import akka.actor.ActorLogging
import akka.persistence.PersistentActor
import com.wingman.RedisClient

case class UpdateState(data: String)
case object GetState

case class State(data: String = "")

class RedisPersistentActor(entityId: String) extends PersistentActor with ActorLogging {

  override def persistenceId: String = s"redis-actor-$entityId"

  private var state: State = State()

  override def receiveCommand: Receive = {
    case UpdateState(data) =>
      log.info(s"Updating state with: $data")
      persist(data) { event =>
        state = State(state.data + event)
        RedisClient.saveState(persistenceId, state.data)
        log.info(s"State updated and persisted in Redis: $state")
      }

    case GetState =>
      log.info(s"Fetching state from Redis for entity: $persistenceId")

      // Retrieve state directly from Redis
      val state = RedisClient.getState(persistenceId).map(State)
      state match {
        case Some(s) =>
          log.info(s"Returning current state from Redis: $s")
          sender() ! s
        case None =>
          log.warning(s"No state found in Redis for entity: $persistenceId")
          sender() ! s"No state found for $persistenceId"
      }

    case other =>
      log.warning(s"Unhandled message: $other")
  }

  override def receiveRecover: Receive = {
    case state: State =>
      log.info(s"Recovering state from journal: $state")
      RedisClient.saveState(persistenceId, state.data)
  }
}

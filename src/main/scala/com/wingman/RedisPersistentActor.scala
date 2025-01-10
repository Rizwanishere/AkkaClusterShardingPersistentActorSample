package com.wingman

import akka.actor.ActorLogging
import akka.persistence.{PersistentActor, SnapshotOffer, SaveSnapshotSuccess, SaveSnapshotFailure}
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

case class UpdateState(data: String)
case object GetState
case object SaveSnapshot
case object ResetState
case class DeleteSnapshots(criteria: String) // For custom deletion if required
case class State @JsonCreator() (@JsonProperty("data") data: String = "")

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
      val redisState = RedisClient.getState(persistenceId).map(State)
      redisState match {
        case Some(s) =>
          log.info(s"Returning current state from Redis: $s")
          sender() ! s
        case None =>
          log.warning(s"No state found in Redis for entity: $persistenceId")
          sender() ! s"No state found for $persistenceId"
      }

    case SaveSnapshot =>
      log.info(s"Saving snapshot for entity: $persistenceId")
      saveSnapshot(state)

    case ResetState =>
      log.info(s"Resetting state for entity: $persistenceId")
      state = State("")
      RedisClient.saveState(persistenceId, state.data)
      log.info(s"State reset to: $state")

    case SaveSnapshotSuccess(metadata) =>
      log.info(s"Snapshot saved successfully: $metadata")

    case SaveSnapshotFailure(metadata, reason) =>
      log.error(s"Failed to save snapshot: $metadata, reason: $reason")

    case SnapshotOffer(metadata, snapshot: State) =>
      log.info(s"Restoring state from snapshot: $metadata")
      state = snapshot
      RedisClient.saveState(persistenceId, state.data)

    case DeleteSnapshots(criteria) =>
      log.warning(s"Custom snapshot deletion requested with criteria: $criteria")
    // Add snapshot deletion logic here based on criteria, if needed

    case other =>
      log.warning(s"Unhandled message: $other")
  }

  override def receiveRecover: Receive = {
    case event: String =>
      log.info(s"Recovering state from journal: $event")
      state = State(state.data + event)
      RedisClient.saveState(persistenceId, state.data)

    case SnapshotOffer(metadata, snapshot: State) =>
      log.info(s"Recovering state from snapshot: $snapshot")
      state = snapshot
  }
}

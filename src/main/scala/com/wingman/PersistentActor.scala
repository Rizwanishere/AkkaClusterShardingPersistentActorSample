package com.wingman

import akka.actor.ActorLogging
import akka.persistence.PersistentActor
import akka.actor.Props
import akka.cluster.sharding.ShardRegion

class PersistentEntityActor(entityId: String) extends PersistentActor with ActorLogging {
  override def persistenceId: String = s"persistent-actor-$entityId"

  private var state = State()

  override def receiveRecover: Receive = {
    case event: DataAdded => updateState(event)
    case snapshot: State  => state = snapshot
  }

  override def receiveCommand: Receive = {
    case AddData(key, value) =>
      val event = DataAdded(key, value)
      persist(event) { persistedEvent =>
        updateState(persistedEvent)
        sender() ! s"Data added: $key -> $value"
      }

    case GetState =>
      sender() ! state
  }

  private def updateState(event: DataAdded): Unit = {
    state = state.copy(data = state.data + (event.key -> event.value))
  }
}

object PersistentEntityActor {
  // Props method to create actor instances with an entityId
  def props(entityId: String): Props = Props(new PersistentEntityActor(entityId))

  // Extract shard ID (based on key hash, 10 shards in this case)
  val extractShardId: ShardRegion.ExtractShardId = {
    case AddData(key, _) => (key.hashCode % 10).toString // 10 shards
    case GetState        => "0" // Default shard
  }

  // Extract entity ID (unique ID for each entity in a shard)
  val extractEntityId: ShardRegion.ExtractEntityId = {
    case AddData(key, value) => (key, AddData(key, value))
    case GetState => ("default", GetState)
  }
}
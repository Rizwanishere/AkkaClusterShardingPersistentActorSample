package com.wingman

import akka.persistence.PersistentActor

// Commands
case class UpdateData(shardKey: String, shardValue: String, payload: String)
case class GetData(shardKey: String, shardValue: String)

// Events
case class DataUpdated(key: String, value: String)

class PersistentEntityActor(entityId: String) extends PersistentActor {

  var state: Map[String, String] = Map.empty

  override def persistenceId: String = s"entity-$entityId"

  override def receiveRecover: Receive = {
    case DataUpdated(key, value) =>
      state += (key -> value)
  }

  override def receiveCommand: Receive = {
    case UpdateData(_, _, payload) =>
      persist(DataUpdated(entityId, payload)) { event =>
        state += (event.key -> event.value)
        sender() ! s"Data persisted: ${event.value}"
      }

    case GetData(_, _) =>
      sender() ! state
  }
}


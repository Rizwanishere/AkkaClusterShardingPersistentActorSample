package com.wingman

import akka.actor.ActorSystem
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}

object MainApp extends App {
  val system = ActorSystem("ClusterSystem")

  val shardRegion = ClusterSharding(system).start(
    typeName = "PersistentEntity",
    entityProps = PersistentEntityActor.props("default"),
    settings = ClusterShardingSettings(system),
    extractShardId = PersistentEntityActor.extractShardId,
    extractEntityId = PersistentEntityActor.extractEntityId
  )

  // Send messages to the shard region
  shardRegion ! AddData("key1", "value1")
  shardRegion ! AddData("key2", "value2")
  shardRegion ! GetState
}

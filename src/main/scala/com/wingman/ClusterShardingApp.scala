package com.wingman

import akka.actor.{ActorSystem, Props}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import akka.cluster.sharding.ShardRegion
import com.wingman.RedisPersistentActor

object ClusterShardingApp extends App {

  val system = ActorSystem("RedisPersistenceApp")

  val extractEntityId: ShardRegion.ExtractEntityId = {
    case (id: String, msg) => (id, msg)
  }

  val extractShardId: ShardRegion.ExtractShardId = {
    case (id: String, _) => (id.hashCode % 100).toString
  }

  ClusterSharding(system).start(
    typeName = "RedisPersistentActor",
    entityProps = Props(classOf[RedisPersistentActor], "2"),
    settings = ClusterShardingSettings(system),
    extractEntityId = extractEntityId,
    extractShardId = extractShardId
  )

  val region = ClusterSharding(system).shardRegion("RedisPersistentActor")
  region ! ("user123", UpdateState("Hello, Akka Persistence with Redis!"))
  region ! ("user123", GetState)
}

package com.wingman

import akka.actor.{ActorSystem, Props}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings, ShardRegion}
import com.wingman.ShardConstants._

object ClusterShardingSetup {

  def main(args: Array[String]): Unit = {
    // Step 1: Initialize ActorSystem
    val system = ActorSystem("ShardClusterSystem")

    // Step 2: Define extractors
    val extractEntityId = extractGenericEntityId()
    val extractShardId = extractGenericShardId(numberOfShards)()

    // Step 3: Start the shard region
    ClusterSharding(system).start(
      typeName = "ShardedEntityActor",
      entityProps = Props[ShardedEntityActor],
      settings = ClusterShardingSettings(system),
      extractEntityId = extractEntityId,
      extractShardId = extractShardId
    )

    // Step 4: Retrieve the shard region
    val shardRegion = ClusterSharding(system).shardRegion("ShardedEntityActor")

    // Step 5: Send test messages
    val testMessages = List(
//      GenericMapRequest(Map(SHARDKEY -> USERID, SHARDVALUE -> "user123")),
//      GenericMapRequest(Map(SHARDKEY -> "order", SHARDVALUE -> "order456")),
//      GenericMapRequest(Map(SHARDKEY -> USERID, SHARDVALUE -> "user789"))

      GenericMapRequest(Map("shardKey" -> "Sohaib", "shardValue" -> "Samad", "operation" -> "update", "value" -> "Bye")),
      GenericMapRequest(Map("shardKey" -> "userId", "shardValue" -> "user123", "operation" -> "get")),
      GenericMapRequest(Map("shardKey" -> "userId", "shardValue" -> "user123", "operation" -> "reset"))
    )

    testMessages.foreach { message =>
      shardRegion ! message
    }
  }

  // Step 6: Implement extractors
  def extractGenericEntityId: () => ShardRegion.ExtractEntityId = {
    lazy val extractEntityId: () => ShardRegion.ExtractEntityId = () => {
      case genericMapRequest: GenericMapRequest =>
        val map = genericMapRequest.fields
        val entityId = if (map.getOrElse(SHARDKEY, "N/A").equalsIgnoreCase(USERID)) {
          val userId = map.getOrElse(SHARDVALUE, "N/A")
          (userId, genericMapRequest)
        } else {
          val shardKey = map.getOrElse(SHARDKEY, "N/A")
          val shardValue = map.getOrElse(SHARDVALUE, "N/A")
          (s"$shardKey:$shardValue", genericMapRequest)
        }
        entityId
    }
    extractEntityId
  }

  def extractGenericShardId(numberOfShards: Int): () => ShardRegion.ExtractShardId = {
    lazy val extractShardId: () => ShardRegion.ExtractShardId = () => {
      case genericMapRequest: GenericMapRequest =>
        val map = genericMapRequest.fields
        if (map.getOrElse(SHARDKEY, "").equalsIgnoreCase(USERID)) {
          val userId = map.getOrElse(SHARDVALUE, "N/A")
          (math.abs(userId.hashCode) % numberOfShards).toString
        } else {
          val id = map.getOrElse(SHARDKEY, "N/A") + ":" + map.getOrElse(SHARDVALUE, "N/A")
          (math.abs(id.hashCode) % numberOfShards).toString
        }
    }
    extractShardId
  }
}

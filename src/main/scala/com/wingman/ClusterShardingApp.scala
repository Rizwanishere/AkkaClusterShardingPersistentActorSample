package com.wingman

import akka.actor.{ActorSystem, Props}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import scala.util.{Failure, Success}

object ClusterShardingApp extends App {

  // Initialize Actor System
  val system = ActorSystem("ClusterShardingSystem")

  // Start Cluster Sharding
  val shardRegion = ClusterSharding(system).start(
    typeName = "PersistentEntity",
    entityProps = Props(new PersistentEntityActor("entityId")),
    settings = ClusterShardingSettings(system),
    extractEntityId = ShardExtractors.extractEntityId,
    extractShardId = ShardExtractors.extractShardId
  )

  // Interact with the Shard Region
  implicit val timeout: Timeout = Timeout(5.seconds)
  implicit val ec = system.dispatcher

  // Update data
  shardRegion ! UpdateData("userId", "user123", "Hello, Akka Persistence!")

  // Retrieve data
  Thread.sleep(1000) // Ensure persistence happens before querying

  val future = (shardRegion ? GetData("userId", "user123")).mapTo[Map[String, String]]
  future.onComplete {
    case Success(data) => println(s"Retrieved data: $data")
    case Failure(ex)   => println(s"Failed to retrieve data: $ex")
  }
}

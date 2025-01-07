package com.wingman

import akka.actor.typed.{ActorSystem, ActorRef}
import akka.actor.typed.scaladsl.{Behaviors}
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity, EntityRef, EntityTypeKey}
import scala.concurrent.duration._

object ClusterShardingApp extends App {

  // Define the EntityTypeKey for MyPersistentActor
  val MyEntityTypeKey = EntityTypeKey[MyPersistentActor.Command]("MyPersistentActor")

  // Create ActorSystem
  val system = ActorSystem[Nothing](
    Behaviors.setup[Nothing] { context =>
      val sharding = ClusterSharding(context.system)

      // Initialize Cluster Sharding with the persistent actor
      sharding.init(Entity(MyEntityTypeKey)(entityContext => MyPersistentActor(entityContext.entityId)))

      // Create an actor to send commands
      val responseActor: ActorRef[MyPersistentActor.Response] = context.spawn(Behaviors.receiveMessage[MyPersistentActor.Response] {
        case MyPersistentActor.Entries(entries) =>
          println(s"Retrieved entries: $entries")
          Behaviors.same
      }, "ResponseActor")

      // Example Usage: Send AddEntry command to a sharded entity
      val entityRef: EntityRef[MyPersistentActor.Command] = sharding.entityRefFor(MyEntityTypeKey, "entity-1")
      entityRef ! MyPersistentActor.AddEntry("Hello, Akka Persistence!")

      // Send GetEntries command after a delay
      context.system.scheduler.scheduleOnce(3.seconds, new Runnable {
        override def run(): Unit = {
          entityRef ! MyPersistentActor.GetEntries(responseActor)
        }
      })(context.executionContext)

      Behaviors.empty
    },
    "ClusterShardingApp"
  )
}

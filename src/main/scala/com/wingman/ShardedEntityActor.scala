package com.wingman

import akka.actor.Actor
import akka.actor.ActorLogging

case class GenericMapRequest(fields: Map[String, String])

class ShardedEntityActor extends Actor with ActorLogging {
  override def receive: Receive = {
    case GenericMapRequest(fields) =>
      println(s"Actor [${self.path.name}] received request with fields: $fields")
      sender() ! s"Handled request for entity with fields: $fields"
  }
}

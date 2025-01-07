package com.wingman

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{Behaviors}

object MyPersistentActor {

  // Commands
  sealed trait Command
  final case class AddEntry(entry: String) extends Command
  final case class GetEntries(replyTo: ActorRef[Response]) extends Command

  // Events
  sealed trait Event
  final case class EntryAdded(entry: String) extends Event

  // State
  final case class State(entries: List[String] = Nil) {
    def addEntry(entry: String): State = copy(entries = entries :+ entry)
  }

  // Responses
  sealed trait Response
  final case class Entries(entries: Seq[String]) extends Response

  def apply(entityId: String): Behavior[Command] = {
    Behaviors.setup { context =>
      context.log.info(s"Starting Persistent Actor for entity [$entityId]")

      Behaviors.receiveMessage {
        case AddEntry(entry) =>
          context.log.info(s"Received AddEntry command with entry: $entry")
          Behaviors.same
        case GetEntries(replyTo) =>
          context.log.info("Received GetEntries command")
          replyTo ! Entries(Seq("Sample entry"))
          Behaviors.same
      }
    }
  }
}

package com.wingman

sealed trait Command

case class AddData(key: String, value: String) extends Command

case object GetState extends Command

sealed trait Event

case class DataAdded(key: String, value: String) extends Event

case class State(data: Map[String, String] = Map.empty)


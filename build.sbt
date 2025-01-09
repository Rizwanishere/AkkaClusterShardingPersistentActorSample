name := "AkkaClusterSharding"

version := "0.1"

scalaVersion := "2.13.12"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.6.20",
  "com.typesafe.akka" %% "akka-cluster-sharding" % "2.6.20",
  "com.typesafe.akka" %% "akka-persistence" % "2.6.20",
  "com.typesafe.akka" %% "akka-persistence-query" % "2.6.20", // Explicitly add this
  "com.typesafe.akka" %% "akka-remote" % "2.6.20",
  "com.typesafe.akka" %% "akka-slf4j" % "2.6.20",
  "ch.qos.logback" % "logback-classic" % "1.2.11"
)

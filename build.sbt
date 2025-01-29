name := "RedisPersistenceApp"

version := "1.0"

scalaVersion := "2.13.12"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-serialization-jackson" % "2.6.20",
  "com.typesafe.akka" %% "akka-actor" % "2.6.20",
  "com.typesafe.akka" %% "akka-cluster" % "2.6.20",
  "com.typesafe.akka" %% "akka-cluster-sharding" % "2.6.20",
  "com.typesafe.akka" %% "akka-persistence" % "2.6.20",
  "com.typesafe.akka" %% "akka-slf4j" % "2.6.20",
  "redis.clients" % "jedis" % "4.4.3", // Redis client
  "ch.qos.logback" % "logback-classic" % "1.2.11",
  "io.suzaku" %% "boopickle" % "1.4.0",
)

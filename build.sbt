val AkkaVersion = "2.6.20"

lazy val root = (project in file("."))
  .settings(
    name := "AkkaPersistenceClusterSharding",
    scalaVersion := "2.13.12",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-cluster-sharding-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-persistence-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-serialization-jackson" % AkkaVersion,
      "com.typesafe.akka" %% "akka-slf4j" % AkkaVersion,
      "com.typesafe.akka" %% "akka-cluster-typed" % AkkaVersion,
      "ch.qos.logback" % "logback-classic" % "1.4.11"
    )
  )

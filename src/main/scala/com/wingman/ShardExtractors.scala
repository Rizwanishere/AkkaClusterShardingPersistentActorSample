package com.wingman

import akka.cluster.sharding.ShardRegion

object ShardExtractors {

  val extractEntityId: ShardRegion.ExtractEntityId = {
    case msg @ UpdateData(_, shardValue, _) => (shardValue, msg)
    case msg @ GetData(_, shardValue)       => (shardValue, msg)
  }

  val extractShardId: ShardRegion.ExtractShardId = {
    case UpdateData(_, shardValue, _) => (shardValue.hashCode % 100).toString
    case GetData(_, shardValue)       => (shardValue.hashCode % 100).toString
  }
}
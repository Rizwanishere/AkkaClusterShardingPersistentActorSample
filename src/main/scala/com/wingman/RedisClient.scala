package com.wingman

import redis.clients.jedis.Jedis

object RedisClient {
  private val jedis = new Jedis("127.0.0.1", 6379)

  def saveState(key: String, state: String): Unit = {
    jedis.set(key, state)
  }

  def getState(key: String): Option[String] = {
    Option(jedis.get(key))
  }
}

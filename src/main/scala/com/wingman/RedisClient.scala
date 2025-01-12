package com.wingman

import redis.clients.jedis.{Jedis, JedisPool, JedisPoolConfig}

object RedisClient {
  // Configure the Jedis pool
  private val poolConfig = new JedisPoolConfig()
  poolConfig.setMaxTotal(128) // Maximum number of connections
  poolConfig.setMaxIdle(16)   // Maximum idle connections
  poolConfig.setMinIdle(4)    // Minimum idle connections
  private val jedisPool = new JedisPool(poolConfig, "127.0.0.1", 6379)

  def saveState(key: String, value: String): Unit = {
    val jedis = jedisPool.getResource
    try {
      jedis.set(key, value)
    } finally {
      jedis.close() // Return the connection to the pool
    }
  }

  def getState(key: String): Option[String] = {
    val jedis = jedisPool.getResource
    try {
      Option(jedis.get(key))
    } finally {
      jedis.close() // Return the connection to the pool
    }
  }
}

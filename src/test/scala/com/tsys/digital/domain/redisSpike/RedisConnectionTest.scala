package com.tsys.digital.domain.redisSpike

import redis.clients.jedis.Jedis;

object RedisConnectionTest extends App {
    val redis = new Jedis("localhost", 6379)
    val response = redis.ping
    println("response = " + response)

    val configInfo = redis.configGet("*")
    println("configInfo = " + configInfo)

    val keys = redis.keys("*")
    System.out.println("keys = " + keys)

    val counter = redis.get("counter")
    println("counter = " + counter)
    redis.incrBy("counter", 10)
    println("counter = " + redis.get("counter"))

    val type_databases = redis.`type`("databases")
    println("type_of_databases = " + type_databases)
    redis.close
}

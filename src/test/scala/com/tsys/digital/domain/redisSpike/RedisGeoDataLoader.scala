package com.tsys.digital.domain.redisSpike

import redis.clients.jedis.Jedis

import scala.util.Try

object RedisGeoDataLoader extends App {
  def parseDouble(s: String) = Try { s.toDouble }.toOption
  def parseInt(s: String) = Try { s.toInt }.toOption

  val redis = new Jedis("localhost", 6379)
  val response = redis.ping
  println("response = " + response)

  println("Removing all keys from current DB = " + redis.flushDB())

  println("Reading Post-Offices in cities long/lat...")
  Try {
    val resource  = getClass.getClassLoader.getResource("data/IN/IN.txt")
    val lines = scala.io.Source.fromURL(resource).mkString.split("\n").toList
    println(s"Read ${lines.size} Post Offices!")
    lines.map(line => {
      val parts = line.split("\t")
      val name = parts(2)
      val state = parts(3)
//      val stateNumber = parseInt(parts(4)).getOrElse(0)
      // Valid longitudes are from -180 to 180 degrees.
      val longitude = parseDouble(parts(10)).getOrElse(-180d)
      // Valid latitudes are from -85.05112878 to 85.05112878 degrees.
      val latitude = parseDouble(parts(9)).getOrElse(-85.05112878)
//      (stateNumber, state, new GeoLocation(longitude, latitude, city))
      (state, GeoLocation(longitude, latitude, name))
    }).groupBy { case (state, pos) => state }
  }.toOption.foreach(posByState =>
    posByState.foreach {
     case (state, pos) => {
      import scala.collection.JavaConverters._
      val posCoordinates = pos.map {
        case (_, po) => po.name -> po.toGeoCoordinate
      }.toMap.asJava
      redis.geoadd(state, posCoordinates)
      println(s"Added Post-Offices of ${state}")
    }
  })
  println("Added all Post Offices!")
  redis.close()
}

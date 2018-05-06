package com.tsys.digital.domain.redisSpike

import redis.clients.jedis.GeoCoordinate

case class PostOffice(state: String, geo: GeoLocation)

case class GeoLocation(longitude: Double, latitude: Double, name: String)  {
  def toGeoCoordinate = new GeoCoordinate(longitude, latitude)
}

package com.tsys.digital.domain

import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class LocationSpecs extends FlatSpec {
  private val longitude: Double = 85.3456789
  private val latitude: Double = 19.92034
  private val id: String = "0"
  private val name: String = "Mumbai Intl. Airport"
  private val city: String = "Mumbai"
  private val state: String = "MH"
  private val country: String = "IN"
  private val `type`: LocationType = LocationType.Airport
  private val mumbaiAirport = new Location(name, city, state, country, `type`, longitude, latitude)

  "A Location" should "create hashKey based on type and id" in {
    assert(mumbaiAirport.copyWithId("1234").hashKey() === "Location:1234")
  }

  it should "create a GeoLocation from Location" in {
    assert(mumbaiAirport.toGeoLocation() === GeoLocation(longitude, latitude, mumbaiAirport.hashKey))
  }

  it should "create a Map representation of Location" in {
    val expected = scala.collection.mutable.Map("id" -> id,
      "name" -> name,
      "city" -> city,
      "state" -> state,
      "country" -> country,
      "type" -> `type`,
      "longitude" -> longitude,
      "latitude" -> latitude)

    import scala.collection.JavaConverters._
    assert(mumbaiAirport.toMap() === expected.asJava)
  }
//  it should "throw NoSuchElementException if an empty stack is popped" in {
//    val emptyStack = new Stack[String]
//    assertThrows[NoSuchElementException] {
//      emptyStack.pop()
//    }
//  }
}

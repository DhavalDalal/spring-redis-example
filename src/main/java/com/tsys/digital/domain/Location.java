package com.tsys.digital.domain;

//import scala.Tuple2;
//import scala.util.parsing.json.JSONObject;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Location implements Serializable {
  private final String id;
  private final String name;
  private final String city;
  private final String state;
  private final String country;
  private final LocationType type;
  private final double longitude;
  private final double latitude;
  private final Date createdOn;

  @Deprecated
  private Location() {
    this.id = "0";
    this.name = "";
    this.city = "";
    this.state = "";
    this.country = "";
    this.type = LocationType.Airport;
    this.longitude = 0;
    this.latitude = 0;
    this.createdOn = new Date();
  }

  private Location(String id, String name, String city, String state, String country, LocationType type, double longitude, double latitude, Date createdOn) {
    this.id = id;
    this.name = name;
    this.city = city;
    this.state = state;
    this.country = country;
    this.type = type;
    this.longitude = longitude;
    this.latitude = latitude;
    this.createdOn = createdOn;
  }

  public Location(String name, String city, String state, String country, LocationType type, Double longitude, Double latitude) {
    this("0", name, city, state, country, type, longitude, latitude, new Date());
  }

  public String hashKey() {
    return id;
//    return hashKeyFor(id);
  }

  public GeoLocation toGeoLocation() {
    return new GeoLocation(longitude, latitude, hashKey());
  }

//  public String toJson() {
//    return JSONObject.apply(toScalaImmutableMap(toMap())).toString();
//  }

//  private <K, V> scala.collection.immutable.Map<K, V> toScalaImmutableMap(java.util.Map<K, V> jmap) {
//    List<Tuple2<K, V>> tuples = jmap.entrySet()
//        .stream()
//        .map(e -> Tuple2.apply(e.getKey(), e.getValue()))
//        .collect(Collectors.toList());
//
//    final scala.collection.Seq<Tuple2<K, V>> seq = scala.collection.JavaConverters.asScalaBufferConverter(tuples).asScala().toSeq();
//    return (scala.collection.immutable.Map<K, V>) scala.collection.immutable.Map$.MODULE$.apply(seq);
//  }

  public Map<String, Object> toMap() {
    return toMap(this);
  }

  private Map<String, Object> toMap(Object ref) {
    return Stream.of(ref.getClass().getDeclaredFields())
        .map(field -> {
          field.setAccessible(true);
          return field;
        })
        .collect(Collectors.toMap(Field::getName, field -> {
          try {
            return field.get(ref);
          } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
          }
        }));
  }

  public Location copyWithId(String id) {
    return new Location(id, name, city, state, country, type, longitude, latitude, createdOn);
  }

  public static Location from(Map<Object, Object> entries) {
    final Set<Object> fieldsActual = entries.keySet();

    final Set<String> fieldsExpected = Stream.of(Location.class.getDeclaredFields())
        .map(field -> {
          field.setAccessible(true);
          return field.getName();
        })
        .collect(toSet());

    if (!fieldsExpected.containsAll(fieldsActual)) {
      throw new RuntimeException("Cannot convert map to " + Location.class.getSimpleName());
    }

    final Location location = new Location();
    Stream.of(Location.class.getDeclaredFields())
        .forEach(field -> {
              field.setAccessible(true);
              try {
                field.set(location, entries.get(field.getName()));
              } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
              }
        });
    return location;
  }

  public String geoKey() {
    return String.format("Country:%s", country);
  }

  public static String hashKeyFor(String id) {
    return id;
//    return String.format("Location:%s", id);
  }

  public boolean hasIdAllocated() {
    return !id.isEmpty() || !id.equals("0");
  }

  @Override
  public String toString() {
    return String.format("Location(%s, %s, %s, %s, %s, %s, %f, %fx, %s)", id, name, city, state, country, type, longitude, latitude, createdOn);
  }
}

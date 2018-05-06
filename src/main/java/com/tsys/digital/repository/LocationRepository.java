package com.tsys.digital.repository;

import com.tsys.digital.domain.Location;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metric;
import org.springframework.data.redis.connection.RedisGeoCommands;

import java.util.List;
import java.util.Optional;

public interface LocationRepository {
  Location saveOrUpdate(Location location);
  Optional<Location> findById(String id);
  Long count();
  Boolean deleteById(String id);
  Page<Location> findAll(Pageable pageable);
  GeoResults<RedisGeoCommands.GeoLocation<Location>> locationsNearby(Double longitude, Double latitude, Distance distance);
  void deleteAll();
  Distance distanceBetween(String id1, String id2, Metric metric);
}

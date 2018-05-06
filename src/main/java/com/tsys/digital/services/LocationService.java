package com.tsys.digital.services;

import com.tsys.digital.domain.Location;
import com.tsys.digital.repository.LocationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LocationService {

  private final LocationRepository locationRepository;

  LocationService(LocationRepository locationRepository) {
    this.locationRepository = locationRepository;
  }

  public Location saveOrUpdate(Location location) {
    return locationRepository.saveOrUpdate(location);
  }

  public GeoResults<RedisGeoCommands.GeoLocation<Location>> locationsNearby(double longitude, double latitude, Distance distance) {
    return locationRepository.locationsNearby(longitude, latitude, distance);
  }

  public Optional<Location> findById(String id) {
    return locationRepository.findById(id);
  }

  public Long count() {
    return locationRepository.count();
  }

  public Boolean deleteById(String id) {
    return locationRepository.deleteById(id);
  }

  public Page<Location> findAll(Pageable pageable) {
    return locationRepository.findAll(pageable);
  }

  public void deleteAll() {
    locationRepository.deleteAll();
  }

  public Distance distanceBetween(String id1, String id2, Metric metric) {
    return locationRepository.distanceBetween(id1, id2, metric);
  }
}

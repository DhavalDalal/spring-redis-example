package com.tsys.digital.repository;

import com.tsys.digital.domain.GeoLocation;
import com.tsys.digital.domain.Location;
import com.tsys.digital.domain.repository.IdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Repository
public class RedisLocationRepository implements LocationRepository {

  private final RedisTemplate<String, Location> redisTemplate;
  private final IdGenerator idGenerator;
  private final GeoOperations<String, String> geoOps;
  private final ZSetOperations<String, Location> zsetOps;
  private final HashOperations<String, String, Location> hashOps;
  private static final String LOCATIONS_KEY = "locations";
  private static final String GEO_LOCATIONS_KEY = "geo-locations";

  @Autowired
  public RedisLocationRepository(RedisTemplate<String, Location> redisTemplate, IdGenerator idGenerator) {
    this.redisTemplate = redisTemplate;
    this.idGenerator = idGenerator;
    this.hashOps = redisTemplate.opsForHash();
    this.geoOps = (GeoOperations<String, String>) hashOps.getOperations().opsForGeo();
    this.zsetOps = redisTemplate.opsForZSet();
  }

  @Override
  public Location saveOrUpdate(Location location) {
    if (location.hasIdAllocated()) {
      saveInRedis(location);
      return location;
    } else {
      final String id = idGenerator.generateId();
      Location idAllocated = location.copyWithId(id);
      saveInRedis(idAllocated);
      return idAllocated;
    }
  }

  private void saveInRedis(Location idAllocated) {
    final GeoLocation geoLocation = idAllocated.toGeoLocation();
    Double longitude = geoLocation.longitude();
    Double latitude = geoLocation.latitude();
    redisTemplate.multi();
    hashOps.put(LOCATIONS_KEY, idAllocated.hashKey(), idAllocated);
    geoOps.add(GEO_LOCATIONS_KEY, new Point(longitude, latitude), idAllocated.hashKey());
//      geoOps.add(idAllocated.geoKey(), new Point(longitude, latitude), idAllocated.hashKey());
    redisTemplate.exec();
  }

  @Override
  public Optional<Location> findById(String id) {
    return Optional.ofNullable(hashOps.get(LOCATIONS_KEY, Location.hashKeyFor(id)));
  }

  @Override
  public Long count() {
    return hashOps.size(LOCATIONS_KEY);
  }

  @Override
  public Boolean deleteById(String id) {
    AtomicBoolean deleted = new AtomicBoolean(false);
    findById(id).ifPresent(location -> {
      deleteInRedis(location);
      deleted.set(true);
    });
    return deleted.get();
  }

  private void deleteInRedis(Location location) {
    redisTemplate.multi();
    geoOps.remove(GEO_LOCATIONS_KEY, location.hashKey());
//    geoOps.remove(location.geoKey(), location.hashKey());
    hashOps.delete(LOCATIONS_KEY, location.hashKey());
    redisTemplate.exec();
  }

  private List<Location> findAll() {
//    ScanOptions scanOptions = ScanOptions.scanOptions()
//        .count(1)
//        .build();
//    final Cursor<Map.Entry<String, Location>> scan = hashOps.scan(LOCATIONS_KEY, scanOptions);
//    scan.
    return hashOps.values(LOCATIONS_KEY);
  }

  @Override
  public Page<Location> findAll(Pageable pageable) {
    if (pageable == Pageable.unpaged()) {
      List<Location> result = findAll();
      return new PageImpl<>(result, Pageable.unpaged(), result.size());
    }

    System.out.println("offSet = " + pageable.getOffset());
    System.out.println("size = " + pageable.getPageSize());

    final List<Location> result = zsetOps.range(LOCATIONS_KEY, pageable.getOffset(), pageable.getOffset() + pageable.getPageSize())
        .stream().collect(Collectors.toList());

    final Long totalElementsInRepo = count();
    System.out.println("totalElementsInRepo = " + totalElementsInRepo);

    return new PageImpl<>(result, pageable, totalElementsInRepo);
  }

  @Override
  public void deleteAll() {
    hashOps.values(LOCATIONS_KEY).forEach(this::deleteInRedis);
  }

  @Override
  public Distance distanceBetween(String id1, String id2, Metric metric) {
    return geoOps.distance(GEO_LOCATIONS_KEY, id1, id2, metric);
  }

  @Override
  public GeoResults<RedisGeoCommands.GeoLocation<Location>> locationsNearby(Double longitude, Double latitude, Distance distance) {
    Circle within = new Circle(new Point(longitude, latitude), distance);
    RedisGeoCommands.GeoRadiusCommandArgs radiusArgs =
        RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
            .includeCoordinates()
            .includeDistance()
            .sortAscending();

    final Spliterator<GeoResult<RedisGeoCommands.GeoLocation<String>>> nearByIds =
        geoOps.radius(GEO_LOCATIONS_KEY, within, radiusArgs).spliterator();

    final List<GeoResult<RedisGeoCommands.GeoLocation<Location>>> nearbyLocations =
        StreamSupport.stream(nearByIds, false)
            .collect(() -> new ArrayList<GeoResult<RedisGeoCommands.GeoLocation<Location>>>(),
                (acc, geoResult) -> {
                  RedisGeoCommands.GeoLocation<String> geoLocation = geoResult.getContent();
                  final Point point = geoLocation.getPoint();
                  final String id = geoLocation.getName();
                  final Location location = hashOps.get(LOCATIONS_KEY, id);
                  final RedisGeoCommands.GeoLocation<Location> locGeoLocation =
                      new RedisGeoCommands.GeoLocation<>(location, point);
                  final GeoResult<RedisGeoCommands.GeoLocation<Location>> result =
                      new GeoResult<>(locGeoLocation, geoResult.getDistance());
                  acc.add(result);
                }, ArrayList::addAll);

    return new GeoResults<>(nearbyLocations);
  }
}
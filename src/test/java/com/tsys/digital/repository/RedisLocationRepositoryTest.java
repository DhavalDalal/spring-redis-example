package com.tsys.digital.repository;

import com.tsys.digital.Application;
import com.tsys.digital.domain.Location;
import com.tsys.digital.domain.LocationType;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.Jedis;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
// ConfigFileApplicationContextInitializer.class is needed for application-test.properties
// to be picked up instead of application-default.properties
@ContextConfiguration(initializers = ConfigFileApplicationContextInitializer.class,
// Classes is needed to merge all the configurations into a single configuration.
    classes = {Application.class, EmbeddedRedisConfiguration.class})
@ActiveProfiles("test")
@DataRedisTest
public class RedisLocationRepositoryTest {

  private static EmbeddedRedisServerRunner redisServerRunner;

  @Autowired
  private Jedis redis;

  @Autowired
  private RedisLocationRepository redisLocationRepository;

  private Location mumbaiAirport = new Location("Mumbai Intl. Airport", "Mumbai", "MH", "IN", LocationType.Airport, 72.8468, 19.2355);
  private Location puneAirport = new Location("Pune Airport", "Pune", "MH", "IN", LocationType.Airport, 75.0311, 18.8199);
  private Location chennaiAirport = new Location("Chennai Airport", "Meenambakam", "TN", "IN", LocationType.Airport, 80.1747, 12.9846);

  @BeforeClass
  public static void startEmbeddedRedisServer() throws InterruptedException {
    final String redisHomeEnv = System.getProperty("redis.home.env");
    System.out.println("redisHome = " + redisHomeEnv);
    final String redisHome = System.getenv(redisHomeEnv);
    System.out.println("redis_home = " + redisHome);
    final String redisExecutablePath = redisHome + System.getProperty("redis.executable");
    System.out.println("redisExecutablePath = " + redisExecutablePath);

    int port = Integer.parseInt(System.getProperty("redis.port"));
    System.out.println("port = " + port);
    File redisExecutable = new File(redisExecutablePath);
    System.out.println("redis executable = " + redisExecutable);
    redisServerRunner = new EmbeddedRedisServerRunner(redisExecutable, port, 500, TimeUnit.MILLISECONDS);
    redisServerRunner.start();
  }

  @AfterClass
  public static void stopEmbeddedRedisServer() throws InterruptedException {
    System.out.println("RedisLocationRepositoryTest.stopEmbeddedRedisServer");
    if (redisServerRunner != null)
      redisServerRunner.stop();
  }

  @Test
  public void connectsToEmbeddedRedisServer() {
    assertEquals("PONG", redis.ping());
  }

  @Test
  public void savesLocation() {
    assertThat(mumbaiAirport.hashKey()).isEqualTo("Location:0");
    final Location saved = redisLocationRepository.saveOrUpdate(mumbaiAirport);
    assertThat(saved.hashKey()).isNotEqualTo("Location:0");
  }

//  @Test
//  public void findsLocationsByLongitudeAndLatitude() {
//    Stream.of(mumbaiAirport, puneAirport, chennaiAirport)
//        .forEach(redisLocationRepository::saveOrUpdate);
//
//    final GeoResults<RedisGeoCommands.GeoLocation<Location>> results = redisLocationRepository.locationsNearby(72.8468, 19.2355, new Distance(200, Metrics.KILOMETERS));
//    System.out.println("results = " + results);
//
//  }
}
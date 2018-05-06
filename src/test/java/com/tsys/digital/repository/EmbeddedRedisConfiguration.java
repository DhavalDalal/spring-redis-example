package com.tsys.digital.repository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.Jedis;

@Configuration
@Profile("test")
public class EmbeddedRedisConfiguration {
  @Bean
  public Jedis redis(@Value("${redis.hostname}") String hostname,
                     @Value("${redis.port}") int port) {
    System.out.println("EmbededRedisConfiguration.redis");
    System.out.println("hostname = " + hostname);
    System.out.println("port = " + port);
    return new Jedis(hostname, port);
  }

  @Bean
  @Primary
  public RedisConnectionFactory redisConnectionFactory(@Value("${redis.hostname}") String hostname,
                                                       @Value("${redis.port}") int port) {
    System.out.println("EmbeddedRedisTestConfiguration.redisConnectionFactory");
    System.out.println("hostname = " + hostname);
    System.out.println("port = " + port);
    RedisStandaloneConfiguration redisStandaloneConfig = new RedisStandaloneConfiguration(hostname, port);
    final JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(redisStandaloneConfig);
    return jedisConnectionFactory;
  }
}

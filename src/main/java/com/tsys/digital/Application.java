package com.tsys.digital;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tsys.digital.domain.Location;
import com.tsys.digital.domain.repository.IdGenerator;
import com.tsys.digital.repository.LocationRepository;
import com.tsys.digital.repository.RedisLocationRepository;
import com.tsys.digital.web.MetricEnumConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.beans.PropertyEditor;

@SpringBootApplication
//@EnableConfigurationProperties
public class Application {

//  @Bean
//  @ConfigurationProperties(prefix ="redis")
//  public RedisConnectionFactory redisConnectionFactory(String hostname,
//                                                       int port) {

	@Bean
	public PropertyEditor metricEnumConverter() {
		return new MetricEnumConverter();
	}

	@Bean
	public PropertyEditor sortDirectionEnumConverter() {
		return new SortDirectionEnumConverter();
	}
  @Bean
  public RedisConnectionFactory redisConnectionFactory(@Value("${redis.hostname}") String hostname,
                                                       @Value("${redis.port}") int port) {
		System.out.println("Application.redisConnectionFactory");
		System.out.println("hostname = " + hostname);
		System.out.println("port = " + port);
		final RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(hostname, port);
    final JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(redisStandaloneConfiguration);
		return jedisConnectionFactory;
  }

  @Bean
	public RedisSerializer<String> stringRedisSerializer() {
  	return new StringRedisSerializer();
	}

	@Bean
	public ObjectMapper jackson2() {
		final ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
		return objectMapper;
	}

	@Bean
	public RedisSerializer<Location> locationRedisSerializer(ObjectMapper jackson2) {
		final Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Location.class);
		jackson2JsonRedisSerializer.setObjectMapper(jackson2);
		return jackson2JsonRedisSerializer;
	}

	@Bean
	public RedisTemplate<String, Location> redisTemplate(RedisConnectionFactory redisConnectionFactory, RedisSerializer<String> stringRedisSerializer, RedisSerializer<Location> locationRedisSerializer) {
		System.out.println("Application.redisTemplate");
		final RedisTemplate<String, Location> template = new RedisTemplate<>();
		template.setKeySerializer(stringRedisSerializer);
		template.setValueSerializer(stringRedisSerializer);
//		template.setValueSerializer(locationRedisSerializer);
		template.setHashKeySerializer(stringRedisSerializer);
		template.setHashValueSerializer(locationRedisSerializer);
		template.setConnectionFactory(redisConnectionFactory);
		template.setEnableTransactionSupport(true);
		return template;
	}

	@Bean
	public LocationRepository redisLocationRepository(RedisTemplate<String, Location> redisTemplate) {
		System.out.println("Application.redisLocationRepository");
		return new RedisLocationRepository(redisTemplate, new IdGenerator());
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}

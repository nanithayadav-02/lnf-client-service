package com.lnf.client.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import redis.clients.jedis.Jedis;

import java.time.Duration;


@Configuration
@Slf4j
@EnableCaching
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private int redisPort;

    @Value("${spring.cache.redis.time-to-live}")
    private long timeToLive;

    @Value("${spring.cache.redis.cache-null-values}")
    private boolean cacheNullValues;

    @Value("${spring.cache.redis.use-key-prefix}")
    private boolean useKeyPrefix;

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        if (isRedisAvailable()) {
            log.info("Using Redis as the cache manager.");
            return RedisCacheManager.builder(redisConnectionFactory)
                    .cacheDefaults(cacheConfiguration())
                    .build();
        } else {
            log.warn("Redis is unavailable. Falling back to in-memory cache.");
            return new ConcurrentMapCacheManager();
        }
    }

    private boolean isRedisAvailable() {
        try (Jedis jedis = new Jedis(redisHost, redisPort)) {
            String response = jedis.ping();
            return "PONG".equalsIgnoreCase(response);
        } catch (Exception e) {
            log.error("Redis connection failed: {}", e.getMessage());
            return false;
        }
    }

    @Bean
    @ConditionalOnBean(name = "redisCacheManager")
    public RedisCacheConfiguration cacheConfiguration() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.activateDefaultTyping(
                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                .entryTtl(Duration.ofMillis(timeToLive));

        if (!cacheNullValues) {
            config = config.disableCachingNullValues();
        }

        if (!useKeyPrefix) {
            config = config.disableKeyPrefix();
        }

        return config;
    }


//Redis config bean without ehcache

//    @Bean
//    public RedisCacheConfiguration cacheConfiguration() {
//        ObjectMapper objectMapper = new ObjectMapper();
//
//        objectMapper.registerModule(new JavaTimeModule());
//
//        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
//        objectMapper.activateDefaultTyping(
//                objectMapper.getPolymorphicTypeValidator(),
//                ObjectMapper.DefaultTyping.NON_FINAL,
//                JsonTypeInfo.As.PROPERTY
//        );
//
//        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);
//        return RedisCacheConfiguration.defaultCacheConfig()
//                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));
//    }
}






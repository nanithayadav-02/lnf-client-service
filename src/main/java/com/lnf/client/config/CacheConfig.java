package com.lnf.client.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.lang.Nullable;
import redis.clients.jedis.Jedis;

import java.time.Duration;


@Configuration
@Slf4j
@EnableCaching
public class CacheConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;
    @Value("${spring.data.redis.port}")
    private int redisPort;
    @Value("${spring.data.cache.redis.time-to-live}")
    private long timeToLive;
    @Value("${spring.data.cache.redis.cache-null-values}")
    private boolean cacheNullValues;
    @Value("${spring.data.cache.redis.use-key-prefix}")
    private boolean useKeyPrefix;

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }

    @Bean
    public CacheManager cacheManager() {
        log.debug("Redis Host: {}, Redis Port: {}", redisHost, redisPort);
        log.debug("Is Redis Available: {}", isRedisAvailable());
        return isRedisAvailable() ? createRedisCacheManager() : new ConcurrentMapCacheManager();

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
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, redisPort);
        return new LettuceConnectionFactory(config);
    }

    private RedisCacheManager createRedisCacheManager() {
        return RedisCacheManager.builder(redisConnectionFactory())
                .cacheDefaults(cacheConfiguration())
                .build();
    }

    public RedisCacheConfiguration cacheConfiguration() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        // Force @class wrapper on root-level collections/maps.
        // NON_FINAL excludes Object.class from typing, so Spring Redis's serialize(Object)
        // never gets a wrapper for ArrayList at the root. writerFor(Collection.class) declares
        // an interface as the root type (interface != runtime class) which forces Jackson to
        // embed the concrete @class so the reader knows what to instantiate.
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper) {
            @Override
            public byte[] serialize(@Nullable Object source) throws SerializationException {
                if (source == null) {
                    return new byte[0];
                }
                try {
                    if (source instanceof java.util.Collection) {
                        return objectMapper.writerFor(java.util.Collection.class).writeValueAsBytes(source);
                    }
                    if (source instanceof java.util.Map) {
                        return objectMapper.writerFor(java.util.Map.class).writeValueAsBytes(source);
                    }
                    return objectMapper.writeValueAsBytes(source);
                } catch (JsonProcessingException e) {
                    throw new SerializationException("Could not write JSON: " + e.getMessage(), e);
                }
            }
        };

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

}

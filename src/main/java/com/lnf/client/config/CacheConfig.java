/*
 *
 *  * Copyright © 2024 Lever And Fulcrum Solutions (hereinafter referred to as "LNF").
 *  * All rights reserved.
 *  *
 *  * This source code is the proprietary property of LNF
 *  *
 *  * Unauthorized copying, redistribution, or modification of this code,
 *  * via any medium, is strictly prohibited unless expressly authorized
 *  * in writing by LNF.
 *  *
 *  * This code is confidential and intended solely for the use of LNF
 *  * and its authorized personnel.
 *
 */

package com.lnf.client.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.Objects;

@Configuration
@EnableCaching
public class CacheConfig {

//    @Bean
//    CacheManager cacheManager() {
//        return new ConcurrentMapCacheManager();
//    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory, ObjectMapper objectMapper) {
        // Register the JavaTimeModule to handle Java 8 Date/Time types
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // Key serializer
        redisTemplate.setKeySerializer(new StringRedisSerializer());

        // Use Jackson2JsonRedisSerializer to handle ProjectDto serialization and deserialization
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
        serializer.setObjectMapper(objectMapper); // Set the custom ObjectMapper with JavaTimeModule
        redisTemplate.setValueSerializer(serializer);

        return redisTemplate;
    }

    @Bean
    @ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis", matchIfMissing = true)
    public RedisCacheManager redisCacheManager(RedisTemplate<String, Object> redisTemplate) {
        RedisCacheWriter cacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(Objects.requireNonNull(redisTemplate.getConnectionFactory()));

        // Ensure that the cache uses the correct serializer
        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(redisTemplate.getValueSerializer()));

        return new RedisCacheManager(cacheWriter, cacheConfig);
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // Set Redis connection factory with timeouts to avoid command timeouts
        LettuceConnectionFactory factory = new LettuceConnectionFactory("localhost", 6379);  // Adjust host/port if needed

        // Set the timeout in milliseconds by converting Duration to long (milliseconds)
        factory.setTimeout(10000L);  // Set a 10-second timeout (10000 milliseconds)

        factory.setValidateConnection(true); // Ensure that Redis is reachable
        return factory;
    }

    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    public CacheManager ehCacheManager() {
        return new EhCacheCacheManager(net.sf.ehcache.CacheManager
                .newInstance(new ClassPathResource("ehcache.xml").getPath()));
    }
}

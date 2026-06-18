package com.lnf.client.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CacheEvictionStartupListener implements ApplicationListener<ApplicationReadyEvent> {

    private final CacheManager cacheManager;

    private static final List<String> CACHE_NAMES = List.of(
            "clients", "projects", "projectOverviewDto", "projectEmployees", "projectTaskEmployees"
    );

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (!(cacheManager instanceof RedisCacheManager)) {
            log.info("Redis not active — skipping cache eviction on startup");
            return;
        }
        log.info("Evicting all client service caches on startup to prevent stale deserialization errors after deploy");
        CACHE_NAMES.forEach(cacheName -> {
            try {
                var cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    cache.clear();
                    log.info("Cleared cache '{}'", cacheName);
                }
            } catch (Exception e) {
                log.warn("Failed to evict cache '{}' on startup: {}", cacheName, e.getMessage());
            }
        });
    }
}

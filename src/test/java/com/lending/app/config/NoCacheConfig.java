package com.lending.app.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.cache.support.NoOpCacheManager;

@TestConfiguration
public class NoCacheConfig {
    @Bean
    @Primary
    public CacheManager cacheManager() {
        return new NoOpCacheManager();
    }
}

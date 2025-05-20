package com.example.notificationservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;
import java.util.Map;

@Configuration
public class CacheConfig {

    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory cf) {
        var defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .entryTtl(Duration.ofMinutes(10));

        Map<String, RedisCacheConfiguration> configs = Map.of(
                "eventDetails", defaultConfig.entryTtl(Duration.ofHours(1)),
                "orderSummaries", defaultConfig.entryTtl(Duration.ofMinutes(5)),
                "customerDetails", defaultConfig.entryTtl(Duration.ofHours(1)),
                "ticketDetails", defaultConfig.entryTtl(Duration.ofHours(1))
        );

        return RedisCacheManager.builder(cf)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(configs)
                .transactionAware()
                .build();
    }
}

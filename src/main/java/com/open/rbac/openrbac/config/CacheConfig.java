package com.open.rbac.openrbac.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    /**
     * Configures a robust in-memory Caffeine cache manager.
     * Caches are cleared after 10 minutes of inactivity and max out at 5000 entries
     * to protect against memory leaks in large tenant deployments.
     */
    @Bean
    @SuppressWarnings("null")
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                "roles",
                "permissions",
                "groups",
                "group_hierarchies",
                "realms",
                "effective_roles",
                "effective_permissions",
                "permission_checks",
                "role_checks",
                "users");

        cacheManager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(5000)
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .recordStats());

        return cacheManager;
    }
}

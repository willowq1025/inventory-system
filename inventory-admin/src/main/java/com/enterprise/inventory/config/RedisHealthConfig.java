package com.enterprise.inventory.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 覆盖 Spring Boot 自带的 Redis 健康指示器。
 *
 * <p>默认的 RedisReactiveHealthIndicator 依赖 INFO 响应解析，在部分 Redis 版本/代理环境下
 * 会抛 "Cannot read Redis info"，把整体健康状态拖成 DOWN，导致容器健康检查误判、实例被反复重启。
 * 这里改用最基础、跨版本稳定的同步 PING 判断连通性；连接可用性由 Lettuce / Redisson 连接池另行保障。
 *
 * <p>bean 名必须是 redisHealthContributor：Spring Boot 的
 * RedisHealthContributorAutoConfiguration 与 RedisReactiveHealthContributorAutoConfiguration
 * 都以该名称上的 @ConditionalOnMissingBean 做退避。
 */
@Configuration
public class RedisHealthConfig {

    @Bean("redisHealthContributor")
    public HealthIndicator redisHealthContributor(StringRedisTemplate template) {
        return () -> {
            try {
                String pong = template.execute((RedisCallback<String>) connection -> connection.ping());
                if ("PONG".equalsIgnoreCase(pong)) {
                    return Health.up().withDetail("ping", "PONG").build();
                }
                return Health.down().withDetail("ping", String.valueOf(pong)).build();
            } catch (Exception e) {
                return Health.down(e).build();
            }
        };
    }
}

package com.enterprise.common.redis.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/** Redisson 用于分布式锁（可重入 + 看门狗自动续期），与 Lettuce 缓存客户端并存。 */
@Configuration
public class RedissonConfig {

    /** 连接池大小：托管 Redis（如 Render Key Value 免费档）并发连接数有限，可通过 REDIS_MAX_ACTIVE 调小 */
    @Value("${inventory.redis.redisson-pool-size:32}")
    private int poolSize;

    @Value("${inventory.redis.redisson-min-idle:4}")
    private int minIdle;

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean(RedissonClient.class)
    public RedissonClient redissonClient(RedisProperties properties) {
        Config config = new Config();
        config.setThreads(4);
        config.setNettyThreads(8);
        // TLS（如 Upstash / 云 Redis rediss://）由 spring.data.redis.ssl.enabled 控制
        boolean ssl = properties.getSsl() != null && properties.getSsl().isEnabled();
        SingleServerConfig server = config.useSingleServer()
                .setAddress((ssl ? "rediss://" : "redis://") + properties.getHost() + ":" + properties.getPort())
                .setDatabase(properties.getDatabase())
                .setConnectionMinimumIdleSize(minIdle)
                .setConnectionPoolSize(poolSize)
                .setIdleConnectionTimeout(60_000)
                .setConnectTimeout(10_000)
                .setTimeout(5_000)
                .setRetryAttempts(3);
        if (StringUtils.hasText(properties.getUsername())) server.setUsername(properties.getUsername());
        if (StringUtils.hasText(properties.getPassword())) server.setPassword(properties.getPassword());
        return Redisson.create(config);
    }
}

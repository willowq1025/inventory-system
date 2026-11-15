package com.enterprise.common.redis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/** 缓存策略配置：inventory.cache.* */
@ConfigurationProperties(prefix = "inventory.cache")
public class CacheProperties {

    /** 默认过期时间 */
    private Duration ttl = Duration.ofMinutes(30);
    /** 空值缓存过期时间（防穿透） */
    private Duration nullTtl = Duration.ofMinutes(2);
    /** 是否缓存空值 */
    private boolean cacheNull = true;
    /** 过期时间随机抖动上限（防雪崩），实际 TTL = ttl + random(0, jitter) */
    private Duration jitter = Duration.ofMinutes(5);
    /** 缓存击穿：回源时是否加互斥锁 */
    private boolean mutexOnMiss = true;
    /** 延迟双删延迟毫秒数（0 关闭） */
    private long doubleDeleteDelayMs = 500;

    public Duration getTtl() { return ttl; }
    public void setTtl(Duration ttl) { this.ttl = ttl; }
    public Duration getNullTtl() { return nullTtl; }
    public void setNullTtl(Duration nullTtl) { this.nullTtl = nullTtl; }
    public boolean isCacheNull() { return cacheNull; }
    public void setCacheNull(boolean cacheNull) { this.cacheNull = cacheNull; }
    public Duration getJitter() { return jitter; }
    public void setJitter(Duration jitter) { this.jitter = jitter; }
    public boolean isMutexOnMiss() { return mutexOnMiss; }
    public void setMutexOnMiss(boolean mutexOnMiss) { this.mutexOnMiss = mutexOnMiss; }
    public long getDoubleDeleteDelayMs() { return doubleDeleteDelayMs; }
    public void setDoubleDeleteDelayMs(long doubleDeleteDelayMs) { this.doubleDeleteDelayMs = doubleDeleteDelayMs; }
}

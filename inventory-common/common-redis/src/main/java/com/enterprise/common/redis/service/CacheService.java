package com.enterprise.common.redis.service;

import com.enterprise.common.core.CacheKey;
import com.enterprise.common.core.util.JsonUtils;
import com.enterprise.common.redis.config.CacheProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * 缓存访问统一入口（Cache-Aside 模式）：
 * 1) TTL 随机抖动，避免大量 key 同时失效导致缓存雪崩；
 * 2) 空值缓存，避免缓存穿透；
 * 3) 回源互斥锁，避免热点 key 失效瞬间的缓存击穿。
 */
@Service
@RequiredArgsConstructor
public class CacheService {

    private final StringRedisTemplate redis;
    private final CacheProperties properties;
    private final DistributedLockTemplate lockTemplate;

    public <T> T get(String key, Class<T> type) {
        String raw = redis.opsForValue().get(key);
        if (raw == null) return null;
        if (CacheKey.NULL_VALUE.equals(raw)) return null;
        return JsonUtils.parse(raw, type);
    }

    public <T> T get(String key, TypeReference<T> type) {
        String raw = redis.opsForValue().get(key);
        if (raw == null || CacheKey.NULL_VALUE.equals(raw)) return null;
        return JsonUtils.parse(raw, type);
    }

    public void set(String key, Object value) {
        set(key, value, properties.getTtl());
    }

    public void set(String key, Object value, Duration ttl) {
        if (value == null) {
            if (properties.isCacheNull()) redis.opsForValue().set(key, CacheKey.NULL_VALUE, properties.getNullTtl());
            else redis.delete(key);
            return;
        }
        redis.opsForValue().set(key, JsonUtils.toJson(value), withJitter(ttl));
    }

    /** 带抖动的 TTL，防雪崩 */
    public Duration withJitter(Duration base) {
        long jitterMs = properties.getJitter().toMillis();
        long extra = jitterMs <= 0 ? 0 : ThreadLocalRandom.current().nextLong(jitterMs);
        return base.plusMillis(extra);
    }

    /**
     * 读缓存 → 未命中回源 → 回填。热点数据建议使用此方法。
     */
    public <T> T getOrLoad(String key, Class<T> type, Supplier<T> loader) {
        return getOrLoad(key, type, properties.getTtl(), loader);
    }

    public <T> T getOrLoad(String key, Class<T> type, Duration ttl, Supplier<T> loader) {
        String raw = redis.opsForValue().get(key);
        if (CacheKey.NULL_VALUE.equals(raw)) return null;
        if (raw != null) return JsonUtils.parse(raw, type);

        if (!properties.isMutexOnMiss()) return loadAndSet(key, ttl, loader);
        return lockTemplate.execute(CacheKey.PREFIX + "lock:miss:" + key, 3, 10, () -> {
            // 双重检查：拿到锁后可能已被其他线程回填
            String again = redis.opsForValue().get(key);
            if (CacheKey.NULL_VALUE.equals(again)) return null;
            if (again != null) return JsonUtils.parse(again, type);
            return loadAndSet(key, ttl, loader);
        });
    }

    private <T> T loadAndSet(String key, Duration ttl, Supplier<T> loader) {
        T value = loader.get();
        set(key, value, ttl);
        return value;
    }

    public void evict(String... keys) {
        if (keys == null || keys.length == 0) return;
        redis.delete(java.util.Arrays.asList(keys));
    }

    /** SCAN 前缀删除，避免 KEYS 命令阻塞 Redis 主线程 */
    public long evictByPrefix(String prefix) {
        Set<String> keys = new HashSet<>();
        try (var cursor = redis.scan(org.springframework.data.redis.core.ScanOptions.scanOptions()
                .match(prefix + "*").count(500).build())) {
            cursor.forEachRemaining(keys::add);
        }
        if (keys.isEmpty()) return 0;
        Long deleted = redis.delete(keys);
        return deleted == null ? 0 : deleted;
    }

    public void evictAll(Collection<String> keys) {
        if (keys != null && !keys.isEmpty()) redis.delete(keys);
    }

    public boolean exists(String key) { return Boolean.TRUE.equals(redis.hasKey(key)); }
}

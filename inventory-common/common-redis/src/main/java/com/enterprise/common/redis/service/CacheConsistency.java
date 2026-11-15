package com.enterprise.common.redis.service;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 缓存一致性保障（Cache-Aside 写路径）：
 *
 * <pre>
 *  1. 先更新数据库；
 *  2. 事务【提交后】再删除缓存 —— 避免事务回滚时缓存已失效、
 *     也避免未提交数据被其他线程读走回填；
 *  3. 可选的延迟双删 —— 处理「读线程把旧值回填到缓存」的极端并发窗口；
 *  4. 无事务时立即删除。
 * </pre>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheConsistency {

    private final CacheService cacheService;
    private final com.enterprise.common.redis.config.CacheProperties properties;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "cache-double-delete");
        t.setDaemon(true);
        return t;
    });

    /** 事务提交后失效指定 key（推荐：所有写操作统一调用） */
    public void evictAfterCommit(String... keys) {
        List<String> targets = Arrays.asList(keys);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() { doEvict(targets); }
            });
        } else {
            doEvict(targets);
        }
    }

    public void evictPrefixAfterCommit(String... prefixes) {
        List<String> targets = Arrays.asList(prefixes);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    targets.forEach(cacheService::evictByPrefix);
                }
            });
        } else {
            targets.forEach(cacheService::evictByPrefix);
        }
    }

    private void doEvict(List<String> keys) {
        keys.forEach(cacheService::evict);
        long delay = properties.getDoubleDeleteDelayMs();
        if (delay > 0) {
            scheduler.schedule(() -> {
                try {
                    keys.forEach(cacheService::evict);
                } catch (Exception e) {
                    log.warn("[cache] 延迟双删失败 keys={}", keys, e);
                }
            }, delay, TimeUnit.MILLISECONDS);
        }
    }

    @PreDestroy
    public void shutdown() { scheduler.shutdownNow(); }
}

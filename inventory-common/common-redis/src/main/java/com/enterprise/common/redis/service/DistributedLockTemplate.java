package com.enterprise.common.redis.service;

import com.enterprise.common.core.BizException;
import com.enterprise.common.core.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/** 编程式分布式锁，供库存扣减、单据状态流转等关键路径使用。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DistributedLockTemplate {

    private final RedissonClient redisson;

    public <T> T execute(String key, long waitSeconds, long leaseSeconds, Supplier<T> action) {
        RLock lock = redisson.getLock(key);
        boolean locked = false;
        try {
            if (leaseSeconds > 0) {
                locked = lock.tryLock(waitSeconds, leaseSeconds, TimeUnit.SECONDS);
            } else {
                // leaseTime <= 0 时启用 Redisson 看门狗，直到业务完成才释放，避免超时误释放
                locked = lock.tryLock(waitSeconds, TimeUnit.SECONDS);
            }
            if (!locked) {
                throw new BizException(ResultCode.CONCURRENT_CONFLICT, "操作正在处理中，请稍后重试");
            }
            return action.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BizException(ResultCode.INTERNAL_ERROR, "获取锁被中断");
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                try { lock.unlock(); } catch (Exception e) { log.warn("[lock] 释放锁异常 key={}", key, e); }
            }
        }
    }

    public void execute(String key, long waitSeconds, long leaseSeconds, Runnable action) {
        execute(key, waitSeconds, leaseSeconds, () -> { action.run(); return null; });
    }
}

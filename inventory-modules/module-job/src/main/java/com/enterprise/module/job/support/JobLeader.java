package com.enterprise.module.job.support;

import com.enterprise.common.core.CacheKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 定时任务集群选主：tryLock(wait=0) 抢到锁的实例才执行，
 * 锁带租约自动过期，实例宕机不会造成任务永久阻塞。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JobLeader {

    private final RedissonClient redisson;

    public boolean runIfLeader(String jobName, long leaseSeconds, Runnable task) {
        RLock lock = redisson.getLock(CacheKey.LOCK_JOB + jobName);
        boolean locked = false;
        try {
            locked = lock.tryLock(0, leaseSeconds, TimeUnit.SECONDS);
            if (!locked) {
                log.debug("[job] {} 未抢到执行权，跳过（集群中其他实例正在执行）", jobName);
                return false;
            }
            long start = System.currentTimeMillis();
            task.run();
            log.info("[job] {} 执行完成，耗时 {}ms", jobName, System.currentTimeMillis() - start);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            log.error("[job] {} 执行失败", jobName, e);
            return false;
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                try { lock.unlock(); } catch (Exception ignored) {}
            }
        }
    }
}

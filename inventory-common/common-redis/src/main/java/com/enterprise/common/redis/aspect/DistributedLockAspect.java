package com.enterprise.common.redis.aspect;

import com.enterprise.common.redis.annotation.DistributedLock;
import com.enterprise.common.redis.service.DistributedLockTemplate;
import com.enterprise.common.redis.util.SpelKeyResolver;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(5)
@RequiredArgsConstructor
public class DistributedLockAspect {

    private final DistributedLockTemplate lockTemplate;

    @Around("@annotation(lock)")
    public Object around(ProceedingJoinPoint joinPoint, DistributedLock lock) throws Throwable {
        String prefix = lock.key();
        String resolved = SpelKeyResolver.resolve(joinPoint, prefix);
        String key = resolved == null ? prefix : resolved;
        long wait = lock.timeUnit().toSeconds(lock.waitTime());
        long lease = lock.leaseTime() <= 0 ? -1 : lock.timeUnit().toSeconds(lock.leaseTime());
        return lockTemplate.execute(key, wait, lease, () -> {
            try {
                return joinPoint.proceed();
            } catch (RuntimeException | Error e) {
                throw e;
            } catch (Throwable t) {
                throw new IllegalStateException(t);
            }
        });
    }
}

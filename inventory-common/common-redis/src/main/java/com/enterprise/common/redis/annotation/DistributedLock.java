package com.enterprise.common.redis.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/** 声明式分布式锁，key 支持 SpEL。 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedLock {

    String key();

    long waitTime() default 5;

    /** <=0 表示启用看门狗自动续期 */
    long leaseTime() default -1;

    TimeUnit timeUnit() default TimeUnit.SECONDS;
}

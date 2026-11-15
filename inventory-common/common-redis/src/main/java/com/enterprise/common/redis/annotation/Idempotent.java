package com.enterprise.common.redis.annotation;

import java.lang.annotation.*;

/**
 * 接口幂等：防止网络重试 / 用户连点造成重复提交。
 * 优先级：SpEL key > 请求头 Idempotency-Key > 用户+方法+参数摘要。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {

    /** 业务模块名，参与 key 命名空间隔离 */
    String module() default "default";

    /** SpEL 表达式，如 "#cmd.orderNo" 或 "#p0.id" */
    String key() default "";

    /** 幂等窗口（秒） */
    long expire() default 120;

    /** 失败时是否立即释放锁（true 表示业务失败允许重试） */
    boolean releaseOnError() default true;

    String message() default "请勿重复提交，请稍后查看结果";
}

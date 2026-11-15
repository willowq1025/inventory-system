package com.enterprise.common.core;

import cn.hutool.core.util.StrUtil;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * 业务断言：替代散落各处的 if-throw，统一异常语义。
 */
public final class BizAssert {

    private BizAssert() {}

    public static void isTrue(boolean expression, String message) {
        if (!expression) throw new BizException(message);
    }

    public static void isTrue(boolean expression, ResultCode code) {
        if (!expression) throw new BizException(code);
    }

    public static void isTrue(boolean expression, ResultCode code, String message) {
        if (!expression) throw new BizException(code, message);
    }

    public static void isFalse(boolean expression, String message) {
        if (expression) throw new BizException(message);
    }

    public static void notNull(Object obj, String message) {
        if (obj == null) throw new BizException(ResultCode.DATA_NOT_FOUND, message);
    }

    public static void notBlank(String str, String message) {
        if (StrUtil.isBlank(str)) throw new BizException(ResultCode.VALIDATION_ERROR, message);
    }

    public static void notEmpty(Collection<?> collection, String message) {
        if (collection == null || collection.isEmpty()) throw new BizException(ResultCode.VALIDATION_ERROR, message);
    }

    public static <T> T requireNotNull(T obj, String message) {
        notNull(obj, message);
        return obj;
    }

    public static void fail(String message) {
        throw new BizException(message);
    }

    public static void fail(ResultCode code, String message) {
        throw new BizException(code, message);
    }

    /** 延迟构造异常消息，避免高并发下无谓的字符串拼接 */
    public static void isTrueLazy(boolean expression, Supplier<String> messageSupplier) {
        if (!expression) throw new BizException(messageSupplier.get());
    }
}

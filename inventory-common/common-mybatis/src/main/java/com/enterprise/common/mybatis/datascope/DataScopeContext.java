package com.enterprise.common.mybatis.datascope;

import org.springframework.core.NamedThreadLocal;

import java.util.function.Supplier;

/** 数据权限忽略标记（登录、字典、统计、系统内部任务等场景）。 */
public final class DataScopeContext {

    private static final ThreadLocal<Boolean> IGNORE = new NamedThreadLocal<>("data-scope-ignore");

    private DataScopeContext() {}

    public static boolean isIgnored() { return Boolean.TRUE.equals(IGNORE.get()); }

    public static void ignore() { IGNORE.set(Boolean.TRUE); }

    public static void clear() { IGNORE.remove(); }

    public static <T> T ignored(Supplier<T> supplier) {
        try {
            ignore();
            return supplier.get();
        } finally {
            clear();
        }
    }

    public static void ignored(Runnable runnable) {
        try {
            ignore();
            runnable.run();
        } finally {
            clear();
        }
    }
}

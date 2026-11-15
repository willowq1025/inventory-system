package com.enterprise.common.core.context;

import org.springframework.core.NamedThreadLocal;

import java.util.function.Supplier;

/**
 * 登录人上下文（ThreadLocal）。
 * 异步线程必须显式使用 wrap / supply 传递，避免父子线程隐式继承导致脏数据。
 */
public final class UserContext {

    private static final ThreadLocal<CurrentUser> HOLDER = new NamedThreadLocal<>("inventory-user-context");

    private UserContext() {}

    public static void set(CurrentUser user) { HOLDER.set(user); }
    public static CurrentUser get() { return HOLDER.get(); }
    public static void clear() { HOLDER.remove(); }

    public static Long userIdOrNull() {
        CurrentUser u = get();
        return u == null ? null : u.getUserId();
    }

    public static Long deptIdOrNull() {
        CurrentUser u = get();
        return u == null ? null : u.getDeptId();
    }

    /** 包装 Runnable，使异步任务继承当前登录人，并在结束后自动清理 */
    public static Runnable wrap(Runnable task) {
        CurrentUser snapshot = get();
        return () -> {
            try {
                set(snapshot);
                task.run();
            } finally {
                clear();
            }
        };
    }

    public static <T> Supplier<T> wrap(Supplier<T> task) {
        CurrentUser snapshot = get();
        return () -> {
            try {
                set(snapshot);
                return task.get();
            } finally {
                clear();
            }
        };
    }

    /** 以指定身份执行（定时任务、系统内部调用） */
    public static void runAs(CurrentUser user, Runnable task) {
        try {
            set(user);
            task.run();
        } finally {
            clear();
        }
    }
}

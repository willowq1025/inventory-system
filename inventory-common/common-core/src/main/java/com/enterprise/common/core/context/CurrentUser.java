package com.enterprise.common.core.context;

import java.util.Set;

/**
 * 当前登录人抽象。common-core 只依赖该接口，
 * 由 common-security 的 LoginUser 提供实现，避免底层模块反向依赖安全实现。
 */
public interface CurrentUser {

    Long getUserId();
    String getUsername();
    String getNickname();
    Long getDeptId();
    Integer getDataScope();
    Set<String> getRoleKeys();
    Set<String> getPermissions();
    /** 自定义数据权限部门（dataScope=5 时生效） */
    Set<Long> getCustomDeptIds();

    default boolean isSuperAdmin() {
        return getRoleKeys() != null && getRoleKeys().contains("admin");
    }

    default boolean hasPermission(String permission) {
        return isSuperAdmin()
                || "*:*:*".equals(permission)
                || (getPermissions() != null && (getPermissions().contains("*:*:*") || getPermissions().contains(permission)));
    }
}

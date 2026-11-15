package com.enterprise.common.security.model;

/** 登录凭据视图（由 module-system 从 DB 提供，不暴露完整实体）。 */
public record UserCredentials(Long userId, String username, String password, boolean enabled, Long deptId) {}

package com.enterprise.common.security.model;

/** 接口权限规则：menu_type=F 且 api_path 非空的菜单行。 */
public record ApiRule(String path, String method, String permission) {}

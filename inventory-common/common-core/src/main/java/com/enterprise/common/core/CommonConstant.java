package com.enterprise.common.core;

/** 全局常量（避免魔法值散落）。 */
public interface CommonConstant {

    String TRACE_ID = "traceId";
    String TRACE_ID_HEADER = "X-Trace-Id";
    String TOKEN_HEADER = "Authorization";
    String TOKEN_PREFIX = "Bearer ";
    String IDEMPOTENCY_HEADER = "Idempotency-Key";

    String ROOT_ID = "0";
    Long ROOT_DEPT_ID = 0L;
    String SUPER_ADMIN_ROLE = "admin";
    String ALL_PERMISSION = "*:*:*";

    int STATUS_ENABLED = 1;
    int STATUS_DISABLED = 0;

    /** 数据权限范围 */
    int SCOPE_ALL = 1;
    int SCOPE_DEPT_AND_CHILD = 2;
    int SCOPE_DEPT = 3;
    int SCOPE_SELF = 4;
    int SCOPE_CUSTOM = 5;

    String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
    String DAY_PATTERN = "yyyy-MM-dd";
}

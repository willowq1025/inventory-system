package com.enterprise.common.mybatis.datascope;

import java.lang.annotation.*;

/** 标注后该方法内所有 SQL 跳过数据权限过滤，仅限公共数据 / 内部任务使用。 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IgnoreDataScope {}

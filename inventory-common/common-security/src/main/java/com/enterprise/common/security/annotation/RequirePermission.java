package com.enterprise.common.security.annotation;

import java.lang.annotation.*;

/** 接口权限标识，可标注在类或方法上；与 sys_menu.permission 对应。 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {

    String[] value();

    Logical logical() default Logical.AND;

    enum Logical { AND, OR }
}

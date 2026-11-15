package com.enterprise.common.log.annotation;

import java.lang.annotation.*;

/** 审计日志注解：标注在需要留痕的写操作上。 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditLog {

    /** 业务标题，如「新增用户」「采购单审核」 */
    String title() default "";

    /** 所属模块 */
    String module() default "";

    /** INSERT / UPDATE / DELETE / AUDIT / IMPORT / EXPORT / LOGIN / OTHER */
    String businessType() default "OTHER";

    boolean saveRequest() default true;

    boolean saveResponse() default false;
}

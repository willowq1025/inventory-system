package com.enterprise.common.security.aspect;

import com.enterprise.common.core.BizException;
import com.enterprise.common.core.ResultCode;
import com.enterprise.common.security.annotation.RequirePermission;
import com.enterprise.common.security.model.LoginUser;
import com.enterprise.common.security.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

/** 就近声明式权限校验（方法注解优先于类注解）。 */
@Slf4j
@Aspect
@Component
@Order(20)
public class PermissionAspect {

    @Around("@annotation(com.enterprise.common.security.annotation.RequirePermission) || "
            + "@within(com.enterprise.common.security.annotation.RequirePermission)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequirePermission annotation = AnnotatedElementUtils.findMergedAnnotation(method, RequirePermission.class);
        if (annotation == null) {
            annotation = AnnotatedElementUtils.findMergedAnnotation(method.getDeclaringClass(), RequirePermission.class);
        }
        if (annotation != null) check(annotation);
        return joinPoint.proceed();
    }

    private void check(RequirePermission annotation) {
        LoginUser user = SecurityUtils.getLoginUser();
        if (user == null) throw new BizException(ResultCode.UNAUTHORIZED);
        if (user.isSuperAdmin()) return;
        String[] required = annotation.value();
        boolean pass = annotation.logical() == RequirePermission.Logical.OR
                ? Arrays.stream(required).anyMatch(user::hasPermission)
                : Arrays.stream(required).allMatch(user::hasPermission);
        if (!pass) {
            log.warn("[auth] 权限校验失败 userId={} required={}", user.getUserId(), Arrays.toString(required));
            throw new BizException(ResultCode.FORBIDDEN, "缺少权限: " + String.join("/", required));
        }
    }
}

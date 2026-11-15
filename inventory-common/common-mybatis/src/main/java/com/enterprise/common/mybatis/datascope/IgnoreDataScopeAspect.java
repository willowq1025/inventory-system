package com.enterprise.common.mybatis.datascope;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(1)
public class IgnoreDataScopeAspect {

    @Around("@annotation(com.enterprise.common.mybatis.datascope.IgnoreDataScope) || "
            + "@within(com.enterprise.common.mybatis.datascope.IgnoreDataScope)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        boolean already = DataScopeContext.isIgnored();
        if (already) return joinPoint.proceed();
        DataScopeContext.ignore();
        try {
            return joinPoint.proceed();
        } finally {
            DataScopeContext.clear();
        }
    }
}

package com.enterprise.common.log.aspect;

import com.enterprise.common.core.util.JsonUtils;
import com.enterprise.common.log.util.LogUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 全局请求日志：记录所有 Controller 的入参、结果、耗时、IP、异常。
 * 生产可通过 logging.level.com.enterprise.common.log.aspect.WebLogAspect=DEBUG 控制入参输出级别。
 */
@Slf4j
@Aspect
@Component
@Order(30)
public class WebLogAspect {

    private static final long SLOW_THRESHOLD_MS = 1500;

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void controllerMethods() {}

    @Around("controllerMethods()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String method = signature.getDeclaringType().getSimpleName() + "." + signature.getMethod().getName();
        HttpServletRequest request = LogUtils.requestOrNull();
        String httpMethod = request == null ? "" : request.getMethod();
        String uri = request == null ? "" : request.getRequestURI();
        String ip = LogUtils.clientIp(request);

        log.debug("[req] {} {} ip={} args={}", httpMethod, uri, ip,
                LogUtils.safeJson(LogUtils.serializableArgs(joinPoint.getArgs())));
        try {
            Object result = joinPoint.proceed();
            long cost = System.currentTimeMillis() - start;
            if (cost > SLOW_THRESHOLD_MS) {
                log.warn("[slow] {} {} 耗时 {}ms", httpMethod, uri, cost);
            } else {
                log.debug("[resp] {} {} cost={}ms result={}", httpMethod, uri, cost,
                        LogUtils.safeJson(result));
            }
            return result;
        } catch (Throwable e) {
            log.error("[error] {} {} cost={}ms ex={} msg={}", httpMethod, uri,
                    System.currentTimeMillis() - start, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        }
    }
}

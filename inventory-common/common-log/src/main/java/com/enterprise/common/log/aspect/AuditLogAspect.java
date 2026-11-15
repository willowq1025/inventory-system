package com.enterprise.common.log.aspect;

import com.enterprise.common.core.context.CurrentUser;
import com.enterprise.common.log.annotation.AuditLog;
import com.enterprise.common.log.model.OperLogEvent;
import com.enterprise.common.log.spi.OperLogRecorder;
import com.enterprise.common.log.util.LogUtils;
import com.enterprise.common.security.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 审计日志切面：成功与失败均留痕，异步落库不影响主链路耗时。
 * 记录内容：操作人/部门/IP/UA/URL/HTTP方法/入参/出参/耗时/状态/异常/traceId。
 */
@Slf4j
@Aspect
@Component
@Order(15)
@RequiredArgsConstructor
public class AuditLogAspect {

    private final Optional<OperLogRecorder> recorder;

    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        long start = System.currentTimeMillis();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        HttpServletRequest request = LogUtils.requestOrNull();
        CurrentUser user = SecurityUtils.currentUser();

        String requestParam = auditLog.saveRequest()
                ? LogUtils.safeJson(LogUtils.serializableArgs(joinPoint.getArgs())) : null;
        Object result = null;
        Throwable failure = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            failure = e;
            throw e;
        } finally {
            try {
                OperLogEvent event = new OperLogEvent(
                        MDC.get("traceId"),
                        auditLog.title(),
                        auditLog.module(),
                        auditLog.businessType(),
                        signature.getDeclaringType().getName() + "." + signature.getMethod().getName(),
                        request == null ? "" : request.getMethod(),
                        request == null ? "" : request.getRequestURI(),
                        requestParam,
                        auditLog.saveResponse() ? LogUtils.safeJson(result) : null,
                        user == null ? null : user.getUserId(),
                        user == null ? "system" : user.getUsername(),
                        user == null ? null : user.getDeptId(),
                        LogUtils.clientIp(request),
                        LogUtils.userAgent(request),
                        failure == null ? 1 : 0,
                        failure == null ? null : LogUtils.truncate(failure.toString(), 1000),
                        System.currentTimeMillis() - start,
                        LocalDateTime.now()
                );
                recorder.ifPresent(r -> r.record(event));
            } catch (Exception e) {
                log.warn("[audit] 审计日志写入失败（不影响业务）", e);
            }
        }
    }
}

package com.enterprise.common.redis.aspect;

import com.enterprise.common.core.BizException;
import com.enterprise.common.core.CacheKey;
import com.enterprise.common.core.ResultCode;
import com.enterprise.common.core.util.JsonUtils;
import com.enterprise.common.core.context.UserContext;
import com.enterprise.common.redis.annotation.Idempotent;
import com.enterprise.common.redis.model.IdempotentRecord;
import com.enterprise.common.redis.spi.IdempotentRecorder;
import com.enterprise.common.redis.util.SpelKeyResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 幂等切面：Redis SET NX 抢占 + 成功响应快照回放 + DB 审计。
 * 与库存扣减的 DB 原子条件更新形成「前置防重 + 底层防超卖」双保险。
 */
@Slf4j
@Aspect
@Component
@Order(10)
@RequiredArgsConstructor
public class IdempotentAspect {

    private static final String PROCESSING = "PROCESSING";
    private static final String SUCCESS = "SUCCESS";

    private final StringRedisTemplate redis;
    private final Optional<IdempotentRecorder> recorder;

    @Around("@annotation(idempotent)")
    public Object around(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        String rawKey = buildKey(joinPoint, idempotent);
        String module = idempotent.module();
        String statusKey = CacheKey.IDEMPOTENT + module + ":" + rawKey;
        String snapshotKey = CacheKey.IDEMPOTENT_SNAPSHOT + module + ":" + rawKey;
        Duration ttl = Duration.ofSeconds(idempotent.expire());
        String requestHash = DigestUtils.md5DigestAsHex(
                JsonUtils.toJson(joinPoint.getArgs()).getBytes(StandardCharsets.UTF_8));

        Boolean acquired = redis.opsForValue().setIfAbsent(statusKey, PROCESSING, ttl);
        if (!Boolean.TRUE.equals(acquired)) {
            String snapshot = redis.opsForValue().get(snapshotKey);
            if (StringUtils.hasText(snapshot)) {
                MethodSignature signature = (MethodSignature) joinPoint.getSignature();
                Class<?> returnType = signature.getReturnType();
                log.info("[idempotent] 命中重复请求，回放首次结果 key={}", rawKey);
                return JsonUtils.parse(snapshot, returnType);
            }
            String status = redis.opsForValue().get(statusKey);
            if (PROCESSING.equals(status)) {
                throw new BizException(ResultCode.REQUEST_IN_PROGRESS, "请求正在处理中，请稍后重试");
            }
            throw new BizException(ResultCode.REPEAT_SUBMIT, idempotent.message());
        }

        try {
            Object result = joinPoint.proceed();
            String json = JsonUtils.toJson(result);
            redis.opsForValue().set(snapshotKey, json, ttl);
            redis.opsForValue().set(statusKey, SUCCESS, ttl);
            persist(module, rawKey, requestHash, json, 1, ttl);
            return result;
        } catch (Throwable e) {
            if (idempotent.releaseOnError()) {
                redis.delete(statusKey);
                redis.delete(snapshotKey);
            }
            persist(module, rawKey, requestHash, null, 2, ttl);
            throw e;
        }
    }

    private void persist(String module, String key, String hash, String response, int status, Duration ttl) {
        recorder.ifPresent(r -> {
            try {
                r.record(new IdempotentRecord(module, key, hash, response, status,
                        LocalDateTime.now().plus(ttl)));
            } catch (Exception ex) {
                log.warn("[idempotent] 落库审计失败 key={}", key, ex);
            }
        });
    }

    private String buildKey(ProceedingJoinPoint joinPoint, Idempotent idempotent) {
        String spel = SpelKeyResolver.resolve(joinPoint, idempotent.key());
        if (StringUtils.hasText(spel)) return spel;

        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            String header = attrs.getRequest().getHeader("Idempotency-Key");
            if (StringUtils.hasText(header)) return header;
        }
        Long userId = UserContext.userIdOrNull();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String args = JsonUtils.toJson(joinPoint.getArgs());
        return (userId == null ? "anonymous" : userId) + ":" + method.getDeclaringClass().getSimpleName()
                + "." + method.getName() + ":" + DigestUtils.md5DigestAsHex(args.getBytes(StandardCharsets.UTF_8));
    }
}

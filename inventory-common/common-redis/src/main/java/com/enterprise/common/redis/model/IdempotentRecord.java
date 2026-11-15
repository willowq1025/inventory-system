package com.enterprise.common.redis.model;

import java.time.LocalDateTime;

/** 幂等记录（SPI 传给业务模块落库审计）。 */
public record IdempotentRecord(
        String module,
        String idempotencyKey,
        String requestHash,
        String responseSnapshot,
        int status,
        LocalDateTime expireTime
) {}

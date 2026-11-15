package com.enterprise.common.redis.spi;

import com.enterprise.common.redis.model.IdempotentRecord;

/**
 * 幂等记录持久化 SPI。Redis 负责快速判重，DB 负责长周期审计与跨机房可追溯。
 * module-system 提供实现（biz_idempotent_record）。
 */
public interface IdempotentRecorder {
    void record(IdempotentRecord record);
}

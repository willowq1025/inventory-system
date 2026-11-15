package com.enterprise.module.system.spi;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.enterprise.common.redis.model.IdempotentRecord;
import com.enterprise.common.redis.spi.IdempotentRecorder;
import com.enterprise.module.system.entity.BizIdempotentRecord;
import com.enterprise.module.system.mapper.BizIdempotentRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/** Redis 负责快速判重，DB 负责长周期审计留存。 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IdempotentRecorderImpl implements IdempotentRecorder {

    private final BizIdempotentRecordMapper mapper;

    @Async
    @Override
    public void record(IdempotentRecord r) {
        try {
            BizIdempotentRecord entity = new BizIdempotentRecord();
            entity.setId(IdWorker.getId());
            entity.setIdempotencyKey(r.idempotencyKey());
            entity.setModule(r.module());
            entity.setRequestHash(r.requestHash());
            entity.setResponseSnapshot(r.responseSnapshot());
            entity.setStatus(r.status());
            entity.setExpireTime(r.expireTime());
            mapper.insert(entity);
        } catch (DuplicateKeyException ignored) {
            // 同一幂等键的多次状态变更只保留首次记录
        } catch (Exception e) {
            log.warn("[idempotent] 审计落库失败 key={}", r.idempotencyKey(), e);
        }
    }
}

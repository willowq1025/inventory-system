package com.enterprise.module.job.job;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.enterprise.module.job.support.JobLeader;
import com.enterprise.module.system.entity.BizIdempotentRecord;
import com.enterprise.module.system.entity.SysOperLog;
import com.enterprise.module.system.mapper.BizIdempotentRecordMapper;
import com.enterprise.module.system.mapper.SysOperLogMapper;
import com.enterprise.module.system.mapper.SysLoginLogMapper;
import com.enterprise.module.system.entity.SysLoginLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/** 数据清理：过期幂等记录 + 90 天前日志，避免审计表无限膨胀。 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CleanupJob {

    private final JobLeader jobLeader;
    private final BizIdempotentRecordMapper idempotentMapper;
    private final SysOperLogMapper operLogMapper;
    private final SysLoginLogMapper loginLogMapper;

    /** 跳过集群选主，供运维手动触发 */
    public void cleanupNested() { doCleanup(); }

    @Scheduled(cron = "${inventory.job.cleanup-cron:0 0 3 * * ?}")
    public void cleanup() {
        jobLeader.runIfLeader("cleanup", 600, this::doCleanup);
    }

    private void doCleanup() {
        {
            LocalDateTime now = LocalDateTime.now();
            int removed = idempotentMapper.deleteExpired(now);
            LocalDateTime logDeadline = now.minusDays(90);
            operLogMapper.delete(Wrappers.lambdaQuery(SysOperLog.class)
                    .lt(SysOperLog::getOperTime, logDeadline));
            loginLogMapper.delete(Wrappers.lambdaQuery(SysLoginLog.class)
                    .lt(SysLoginLog::getLoginTime, logDeadline));
            log.info("[job] 清理过期幂等记录 {} 条，日志截止 {}", removed, logDeadline);
        }
    }
}

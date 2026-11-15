package com.enterprise.module.system.spi;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.enterprise.common.log.model.OperLogEvent;
import com.enterprise.common.log.spi.OperLogRecorder;
import com.enterprise.module.system.entity.SysOperLog;
import com.enterprise.module.system.mapper.SysOperLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/** 审计日志异步落库，失败只告警不影响业务。 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OperLogRecorderImpl implements OperLogRecorder {

    private final SysOperLogMapper mapper;

    @Async
    @Override
    public void record(OperLogEvent e) {
        try {
            SysOperLog entity = new SysOperLog();
            entity.setId(IdWorker.getId());
            entity.setTraceId(e.traceId());
            entity.setTitle(e.title());
            entity.setModule(e.module());
            entity.setBusinessType(e.businessType());
            entity.setMethod(e.method());
            entity.setRequestMethod(e.requestMethod());
            entity.setRequestUrl(e.requestUrl());
            entity.setRequestParam(e.requestParam());
            entity.setResponseResult(e.responseResult());
            entity.setOperId(e.operId());
            entity.setOperName(e.operName());
            entity.setOperDept(e.operDept());
            entity.setOperIp(e.operIp());
            entity.setUserAgent(e.userAgent());
            entity.setStatus(e.status());
            entity.setErrorMsg(e.errorMsg());
            entity.setCostMs(e.costMs());
            entity.setOperTime(e.operTime());
            mapper.insert(entity);
        } catch (Exception ex) {
            log.error("[audit] 审计日志落库失败 title={}", e.title(), ex);
        }
    }
}

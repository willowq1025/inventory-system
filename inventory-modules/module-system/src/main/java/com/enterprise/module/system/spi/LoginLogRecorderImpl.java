package com.enterprise.module.system.spi;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.enterprise.common.log.model.LoginLogEvent;
import com.enterprise.common.log.spi.LoginLogRecorder;
import com.enterprise.module.system.entity.SysLoginLog;
import com.enterprise.module.system.mapper.SysLoginLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginLogRecorderImpl implements LoginLogRecorder {

    private final SysLoginLogMapper mapper;

    @Async
    @Override
    public void record(LoginLogEvent e) {
        try {
            SysLoginLog entity = new SysLoginLog();
            entity.setId(IdWorker.getId());
            entity.setTraceId(e.traceId());
            entity.setUsername(e.username());
            entity.setIp(e.ip());
            entity.setBrowser(e.browser());
            entity.setOs(e.os());
            entity.setStatus(e.status());
            entity.setMessage(e.message());
            entity.setLoginTime(e.loginTime());
            mapper.insert(entity);
        } catch (Exception ex) {
            log.error("[audit] 登录日志落库失败 user={}", e.username(), ex);
        }
    }
}

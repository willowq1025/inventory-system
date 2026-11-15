package com.enterprise.common.log.spi;

import com.enterprise.common.log.model.LoginLogEvent;

/** 登录日志落库 SPI，module-system 实现（sys_login_log）。 */
public interface LoginLogRecorder {
    void record(LoginLogEvent event);
}

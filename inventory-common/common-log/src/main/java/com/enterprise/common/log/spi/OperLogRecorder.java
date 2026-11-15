package com.enterprise.common.log.spi;

import com.enterprise.common.log.model.OperLogEvent;

/** 审计日志落库 SPI，module-system 实现（sys_oper_log）。 */
public interface OperLogRecorder {
    void record(OperLogEvent event);
}

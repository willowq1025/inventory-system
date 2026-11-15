package com.enterprise.common.log.model;

import java.time.LocalDateTime;

public record LoginLogEvent(
        String traceId,
        String username,
        String ip,
        String browser,
        String os,
        int status,
        String message,
        LocalDateTime loginTime
) {}

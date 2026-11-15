package com.enterprise.common.log.model;

import java.time.LocalDateTime;

/** 审计日志事件：谁、在什么时间、通过什么接口、对什么数据、做了什么、结果如何。 */
public record OperLogEvent(
        String traceId,
        String title,
        String module,
        String businessType,
        String method,
        String requestMethod,
        String requestUrl,
        String requestParam,
        String responseResult,
        Long operId,
        String operName,
        Long operDept,
        String operIp,
        String userAgent,
        int status,
        String errorMsg,
        long costMs,
        LocalDateTime operTime
) {}

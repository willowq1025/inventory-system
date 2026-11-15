package com.enterprise.common.log.config;

import com.enterprise.common.core.CommonConstant;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * 全链路 traceId：优先复用网关/前端透传的 X-Trace-Id，否则生成；
 * 写入 MDC 供 logback 输出，并回写响应头，前端报错时可直接带上该 ID 报障。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String traceId = request.getHeader(CommonConstant.TRACE_ID_HEADER);
        if (!StringUtils.hasText(traceId)) traceId = UUID.randomUUID().toString().replace("-", "");
        MDC.put(CommonConstant.TRACE_ID, traceId);
        response.setHeader(CommonConstant.TRACE_ID_HEADER, traceId);
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}

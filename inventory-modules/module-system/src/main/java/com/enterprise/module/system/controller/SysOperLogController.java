package com.enterprise.module.system.controller;

import com.enterprise.common.core.PageResult;
import com.enterprise.common.core.Result;
import com.enterprise.common.log.annotation.AuditLog;
import com.enterprise.common.redis.annotation.Idempotent;
import com.enterprise.common.security.annotation.RequirePermission;
import com.enterprise.module.system.dto.SysOperLogQuery;
import com.enterprise.module.system.entity.SysOperLog;
import com.enterprise.module.system.service.SysOperLogService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 审计日志管理接口。
 * 权限：@RequirePermission + sys_menu 中登记的接口规则；
 * 审计：@AuditLog 自动记录操作人/时间/入参；
 * 幂等：@Idempotent 防止重复提交。
 */
@Validated
@RestController
@RequestMapping("/api/system/oper-logs")
@RequiredArgsConstructor
public class SysOperLogController {

    private final SysOperLogService service;

    @GetMapping
    @RequirePermission("system:log:list")
    public Result<PageResult<SysOperLog>> page(@Valid SysOperLogQuery query) {
        return Result.ok(service.page(query));
    }

    @GetMapping("/{id}")
    @RequirePermission("system:log:list")
    public Result<SysOperLog> detail(@PathVariable Long id) {
        return Result.ok(service.detail(id));
    }

    @GetMapping("/export")
    @RequirePermission("system:log:export")
    @AuditLog(title = "导出审计日志", module = "审计日志", businessType = "EXPORT")
    public void export(@Valid SysOperLogQuery query, HttpServletResponse response) {
        service.export(query, response);
    }

}

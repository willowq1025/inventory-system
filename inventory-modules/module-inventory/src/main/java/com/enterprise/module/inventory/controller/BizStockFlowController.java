package com.enterprise.module.inventory.controller;

import com.enterprise.common.core.PageResult;
import com.enterprise.common.core.Result;
import com.enterprise.common.log.annotation.AuditLog;
import com.enterprise.common.redis.annotation.Idempotent;
import com.enterprise.common.security.annotation.RequirePermission;
import com.enterprise.module.inventory.dto.BizStockFlowQuery;
import com.enterprise.module.inventory.entity.BizStockFlow;
import com.enterprise.module.inventory.service.BizStockFlowService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 库存流水管理接口。
 * 权限：@RequirePermission + sys_menu 中登记的接口规则；
 * 审计：@AuditLog 自动记录操作人/时间/入参；
 * 幂等：@Idempotent 防止重复提交。
 */
@Validated
@RestController
@RequestMapping("/api/inventory/flows")
@RequiredArgsConstructor
public class BizStockFlowController {

    private final BizStockFlowService service;

    @GetMapping
    @RequirePermission("inventory:flow:list")
    public Result<PageResult<BizStockFlow>> page(@Valid BizStockFlowQuery query) {
        return Result.ok(service.page(query));
    }

    @GetMapping("/{id}")
    @RequirePermission("inventory:flow:list")
    public Result<BizStockFlow> detail(@PathVariable Long id) {
        return Result.ok(service.detail(id));
    }

    @GetMapping("/export")
    @RequirePermission("inventory:flow:export")
    @AuditLog(title = "导出库存流水", module = "库存流水", businessType = "EXPORT")
    public void export(@Valid BizStockFlowQuery query, HttpServletResponse response) {
        service.export(query, response);
    }

}

package com.enterprise.module.inventory.controller;

import com.enterprise.common.core.PageResult;
import com.enterprise.common.core.Result;
import com.enterprise.common.log.annotation.AuditLog;
import com.enterprise.common.redis.annotation.Idempotent;
import com.enterprise.common.security.annotation.RequirePermission;
import com.enterprise.module.inventory.dto.TransferCmd;
import com.enterprise.module.inventory.entity.BizTransfer;
import com.enterprise.module.inventory.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/inventory/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService service;

    @GetMapping
    @RequirePermission("inventory:transfer:list")
    public Result<PageResult<BizTransfer>> page(@RequestParam(defaultValue = "1") long pageNum,
                                                 @RequestParam(defaultValue = "20") long pageSize,
                                                 @RequestParam(required = false) Integer status) {
        return Result.ok(service.page(pageNum, pageSize, status));
    }

    @PostMapping
    @RequirePermission("inventory:transfer:create")
    @AuditLog(title = "创建调拨单", module = "库存管理", businessType = "INSERT")
    @Idempotent(module = "transfer", expire = 60)
    public Result<Long> create(@Valid @RequestBody TransferCmd cmd) {
        return Result.ok(service.create(cmd));
    }

    @PostMapping("/{id}/audit")
    @RequirePermission("inventory:transfer:audit")
    @AuditLog(title = "审核调拨单", module = "库存管理", businessType = "AUDIT")
    @Idempotent(module = "transfer-audit", key = "#id", expire = 300)
    public Result<Void> audit(@PathVariable("id") Long id) {
        service.audit(id);
        return Result.ok();
    }
}

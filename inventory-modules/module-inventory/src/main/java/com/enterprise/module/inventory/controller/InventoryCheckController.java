package com.enterprise.module.inventory.controller;

import com.enterprise.common.core.PageResult;
import com.enterprise.common.core.Result;
import com.enterprise.common.log.annotation.AuditLog;
import com.enterprise.common.redis.annotation.Idempotent;
import com.enterprise.common.security.annotation.RequirePermission;
import com.enterprise.module.inventory.dto.CheckCmd;
import com.enterprise.module.inventory.entity.BizInventoryCheck;
import com.enterprise.module.inventory.entity.BizInventoryCheckItem;
import com.enterprise.module.inventory.service.InventoryCheckService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/inventory/checks")
@RequiredArgsConstructor
public class InventoryCheckController {

    private final InventoryCheckService service;

    @GetMapping
    @RequirePermission("inventory:check:list")
    public Result<PageResult<BizInventoryCheck>> page(@RequestParam(defaultValue = "1") long pageNum,
                                                       @RequestParam(defaultValue = "20") long pageSize,
                                                       @RequestParam(required = false) Integer status) {
        return Result.ok(service.page(pageNum, pageSize, status));
    }

    @GetMapping("/{id}/items")
    @RequirePermission("inventory:check:list")
    public Result<List<BizInventoryCheckItem>> items(@PathVariable Long id) {
        return Result.ok(service.items(id));
    }

    @PostMapping
    @RequirePermission("inventory:check:create")
    @AuditLog(title = "创建盘点单", module = "库存管理", businessType = "INSERT")
    @Idempotent(module = "check", expire = 60)
    public Result<Long> create(@Valid @RequestBody CheckCmd cmd) {
        return Result.ok(service.create(cmd));
    }

    @PostMapping("/{id}/audit")
    @RequirePermission("inventory:check:audit")
    @AuditLog(title = "审核盘点单", module = "库存管理", businessType = "AUDIT")
    @Idempotent(module = "check-audit", key = "#id", expire = 300)
    public Result<Void> audit(@PathVariable("id") Long id) {
        service.audit(id);
        return Result.ok();
    }
}

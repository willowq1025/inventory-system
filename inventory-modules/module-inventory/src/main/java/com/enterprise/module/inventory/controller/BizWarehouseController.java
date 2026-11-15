package com.enterprise.module.inventory.controller;

import com.enterprise.common.core.PageResult;
import com.enterprise.common.core.Result;
import com.enterprise.common.excel.model.ExcelImportResult;
import com.enterprise.common.log.annotation.AuditLog;
import com.enterprise.common.redis.annotation.Idempotent;
import com.enterprise.common.security.annotation.RequirePermission;
import com.enterprise.module.inventory.dto.BizWarehouseQuery;
import com.enterprise.module.inventory.entity.BizWarehouse;
import com.enterprise.module.inventory.service.BizWarehouseService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 仓库管理接口。
 * 权限：@RequirePermission + sys_menu 中登记的接口规则；
 * 审计：@AuditLog 自动记录操作人/时间/入参；
 * 幂等：@Idempotent 防止重复提交。
 */
@Validated
@RestController
@RequestMapping("/api/base/warehouses")
@RequiredArgsConstructor
public class BizWarehouseController {

    private final BizWarehouseService service;

    @GetMapping
    @RequirePermission("base:warehouse:list")
    public Result<PageResult<BizWarehouse>> page(@Valid BizWarehouseQuery query) {
        return Result.ok(service.page(query));
    }

    @GetMapping("/{id}")
    @RequirePermission("base:warehouse:list")
    public Result<BizWarehouse> detail(@PathVariable Long id) {
        return Result.ok(service.detail(id));
    }

    @PostMapping
    @RequirePermission("base:warehouse:create")
    @AuditLog(title = "新增仓库", module = "仓库", businessType = "INSERT", saveResponse = true)
    @Idempotent(module = "BizWarehouse", expire = 30)
    public Result<Long> create(@Valid @RequestBody BizWarehouse entity) {
        return Result.ok(service.create(entity));
    }

    @PutMapping("/{id}")
    @RequirePermission("base:warehouse:update")
    @AuditLog(title = "修改仓库", module = "仓库", businessType = "UPDATE")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody BizWarehouse entity) {
        entity.setId(id);
        service.update(entity);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @RequirePermission("base:warehouse:delete")
    @AuditLog(title = "删除仓库", module = "仓库", businessType = "DELETE")
    public Result<Void> remove(@PathVariable Long id) {
        service.remove(id);
        return Result.ok();
    }

    @GetMapping("/export")
    @RequirePermission("base:warehouse:export")
    @AuditLog(title = "导出仓库", module = "仓库", businessType = "EXPORT")
    public void export(@Valid BizWarehouseQuery query, HttpServletResponse response) {
        service.export(query, response);
    }

    @PostMapping("/import")
    @RequirePermission("base:warehouse:import")
    @AuditLog(title = "导入仓库", module = "仓库", businessType = "IMPORT", saveResponse = true)
    public Result<ExcelImportResult> importData(@RequestParam("file") MultipartFile file) {
        return Result.ok(service.importData(file));
    }

}

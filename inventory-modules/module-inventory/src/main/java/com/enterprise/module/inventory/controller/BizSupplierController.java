package com.enterprise.module.inventory.controller;

import com.enterprise.common.core.PageResult;
import com.enterprise.common.core.Result;
import com.enterprise.common.excel.model.ExcelImportResult;
import com.enterprise.common.log.annotation.AuditLog;
import com.enterprise.common.redis.annotation.Idempotent;
import com.enterprise.common.security.annotation.RequirePermission;
import com.enterprise.module.inventory.dto.BizSupplierQuery;
import com.enterprise.module.inventory.entity.BizSupplier;
import com.enterprise.module.inventory.service.BizSupplierService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 供应商管理接口。
 * 权限：@RequirePermission + sys_menu 中登记的接口规则；
 * 审计：@AuditLog 自动记录操作人/时间/入参；
 * 幂等：@Idempotent 防止重复提交。
 */
@Validated
@RestController
@RequestMapping("/api/base/suppliers")
@RequiredArgsConstructor
public class BizSupplierController {

    private final BizSupplierService service;

    @GetMapping
    @RequirePermission("base:supplier:list")
    public Result<PageResult<BizSupplier>> page(@Valid BizSupplierQuery query) {
        return Result.ok(service.page(query));
    }

    @GetMapping("/{id}")
    @RequirePermission("base:supplier:list")
    public Result<BizSupplier> detail(@PathVariable Long id) {
        return Result.ok(service.detail(id));
    }

    @PostMapping
    @RequirePermission("base:supplier:create")
    @AuditLog(title = "新增供应商", module = "供应商", businessType = "INSERT", saveResponse = true)
    @Idempotent(module = "BizSupplier", expire = 30)
    public Result<Long> create(@Valid @RequestBody BizSupplier entity) {
        return Result.ok(service.create(entity));
    }

    @PutMapping("/{id}")
    @RequirePermission("base:supplier:update")
    @AuditLog(title = "修改供应商", module = "供应商", businessType = "UPDATE")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody BizSupplier entity) {
        entity.setId(id);
        service.update(entity);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @RequirePermission("base:supplier:delete")
    @AuditLog(title = "删除供应商", module = "供应商", businessType = "DELETE")
    public Result<Void> remove(@PathVariable Long id) {
        service.remove(id);
        return Result.ok();
    }

    @GetMapping("/export")
    @RequirePermission("base:supplier:export")
    @AuditLog(title = "导出供应商", module = "供应商", businessType = "EXPORT")
    public void export(@Valid BizSupplierQuery query, HttpServletResponse response) {
        service.export(query, response);
    }

    @PostMapping("/import")
    @RequirePermission("base:supplier:import")
    @AuditLog(title = "导入供应商", module = "供应商", businessType = "IMPORT", saveResponse = true)
    public Result<ExcelImportResult> importData(@RequestParam("file") MultipartFile file) {
        return Result.ok(service.importData(file));
    }

}

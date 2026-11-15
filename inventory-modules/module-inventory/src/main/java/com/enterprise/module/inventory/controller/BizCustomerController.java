package com.enterprise.module.inventory.controller;

import com.enterprise.common.core.PageResult;
import com.enterprise.common.core.Result;
import com.enterprise.common.excel.model.ExcelImportResult;
import com.enterprise.common.log.annotation.AuditLog;
import com.enterprise.common.redis.annotation.Idempotent;
import com.enterprise.common.security.annotation.RequirePermission;
import com.enterprise.module.inventory.dto.BizCustomerQuery;
import com.enterprise.module.inventory.entity.BizCustomer;
import com.enterprise.module.inventory.service.BizCustomerService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 客户管理接口。
 * 权限：@RequirePermission + sys_menu 中登记的接口规则；
 * 审计：@AuditLog 自动记录操作人/时间/入参；
 * 幂等：@Idempotent 防止重复提交。
 */
@Validated
@RestController
@RequestMapping("/api/base/customers")
@RequiredArgsConstructor
public class BizCustomerController {

    private final BizCustomerService service;

    @GetMapping
    @RequirePermission("base:customer:list")
    public Result<PageResult<BizCustomer>> page(@Valid BizCustomerQuery query) {
        return Result.ok(service.page(query));
    }

    @GetMapping("/{id}")
    @RequirePermission("base:customer:list")
    public Result<BizCustomer> detail(@PathVariable Long id) {
        return Result.ok(service.detail(id));
    }

    @PostMapping
    @RequirePermission("base:customer:create")
    @AuditLog(title = "新增客户", module = "客户", businessType = "INSERT", saveResponse = true)
    @Idempotent(module = "BizCustomer", expire = 30)
    public Result<Long> create(@Valid @RequestBody BizCustomer entity) {
        return Result.ok(service.create(entity));
    }

    @PutMapping("/{id}")
    @RequirePermission("base:customer:update")
    @AuditLog(title = "修改客户", module = "客户", businessType = "UPDATE")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody BizCustomer entity) {
        entity.setId(id);
        service.update(entity);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @RequirePermission("base:customer:delete")
    @AuditLog(title = "删除客户", module = "客户", businessType = "DELETE")
    public Result<Void> remove(@PathVariable Long id) {
        service.remove(id);
        return Result.ok();
    }

    @GetMapping("/export")
    @RequirePermission("base:customer:export")
    @AuditLog(title = "导出客户", module = "客户", businessType = "EXPORT")
    public void export(@Valid BizCustomerQuery query, HttpServletResponse response) {
        service.export(query, response);
    }

    @PostMapping("/import")
    @RequirePermission("base:customer:import")
    @AuditLog(title = "导入客户", module = "客户", businessType = "IMPORT", saveResponse = true)
    public Result<ExcelImportResult> importData(@RequestParam("file") MultipartFile file) {
        return Result.ok(service.importData(file));
    }

}

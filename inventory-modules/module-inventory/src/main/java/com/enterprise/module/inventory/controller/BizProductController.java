package com.enterprise.module.inventory.controller;

import com.enterprise.common.core.PageResult;
import com.enterprise.common.core.Result;
import com.enterprise.common.excel.model.ExcelImportResult;
import com.enterprise.common.log.annotation.AuditLog;
import com.enterprise.common.redis.annotation.Idempotent;
import com.enterprise.common.security.annotation.RequirePermission;
import com.enterprise.module.inventory.dto.BizProductQuery;
import com.enterprise.module.inventory.entity.BizProduct;
import com.enterprise.module.inventory.service.BizProductService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 商品管理接口。
 * 权限：@RequirePermission + sys_menu 中登记的接口规则；
 * 审计：@AuditLog 自动记录操作人/时间/入参；
 * 幂等：@Idempotent 防止重复提交。
 */
@Validated
@RestController
@RequestMapping("/api/base/products")
@RequiredArgsConstructor
public class BizProductController {

    private final BizProductService service;

    @GetMapping
    @RequirePermission("base:product:list")
    public Result<PageResult<BizProduct>> page(@Valid BizProductQuery query) {
        return Result.ok(service.page(query));
    }

    @GetMapping("/{id}")
    @RequirePermission("base:product:list")
    public Result<BizProduct> detail(@PathVariable Long id) {
        return Result.ok(service.detail(id));
    }

    @PostMapping
    @RequirePermission("base:product:create")
    @AuditLog(title = "新增商品", module = "商品", businessType = "INSERT", saveResponse = true)
    @Idempotent(module = "BizProduct", expire = 30)
    public Result<Long> create(@Valid @RequestBody BizProduct entity) {
        return Result.ok(service.create(entity));
    }

    @PutMapping("/{id}")
    @RequirePermission("base:product:update")
    @AuditLog(title = "修改商品", module = "商品", businessType = "UPDATE")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody BizProduct entity) {
        entity.setId(id);
        service.update(entity);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @RequirePermission("base:product:delete")
    @AuditLog(title = "删除商品", module = "商品", businessType = "DELETE")
    public Result<Void> remove(@PathVariable Long id) {
        service.remove(id);
        return Result.ok();
    }

    @GetMapping("/export")
    @RequirePermission("base:product:export")
    @AuditLog(title = "导出商品", module = "商品", businessType = "EXPORT")
    public void export(@Valid BizProductQuery query, HttpServletResponse response) {
        service.export(query, response);
    }

    @PostMapping("/import")
    @RequirePermission("base:product:import")
    @AuditLog(title = "导入商品", module = "商品", businessType = "IMPORT", saveResponse = true)
    public Result<ExcelImportResult> importData(@RequestParam("file") MultipartFile file) {
        return Result.ok(service.importData(file));
    }

}

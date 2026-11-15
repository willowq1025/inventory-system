package com.enterprise.module.inventory.controller;

import com.enterprise.common.core.PageResult;
import com.enterprise.common.core.Result;
import com.enterprise.common.excel.model.ExcelImportResult;
import com.enterprise.common.log.annotation.AuditLog;
import com.enterprise.common.redis.annotation.Idempotent;
import com.enterprise.common.security.annotation.RequirePermission;
import com.enterprise.module.inventory.dto.BizCategoryQuery;
import com.enterprise.module.inventory.entity.BizCategory;
import com.enterprise.module.inventory.service.BizCategoryService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 商品分类管理接口。
 * 权限：@RequirePermission + sys_menu 中登记的接口规则；
 * 审计：@AuditLog 自动记录操作人/时间/入参；
 * 幂等：@Idempotent 防止重复提交。
 */
@Validated
@RestController
@RequestMapping("/api/base/categories")
@RequiredArgsConstructor
public class BizCategoryController {

    private final BizCategoryService service;

    @GetMapping
    @RequirePermission("base:category:list")
    public Result<PageResult<BizCategory>> page(@Valid BizCategoryQuery query) {
        return Result.ok(service.page(query));
    }

    @GetMapping("/{id}")
    @RequirePermission("base:category:list")
    public Result<BizCategory> detail(@PathVariable Long id) {
        return Result.ok(service.detail(id));
    }

    @PostMapping
    @RequirePermission("base:category:create")
    @AuditLog(title = "新增商品分类", module = "商品分类", businessType = "INSERT", saveResponse = true)
    @Idempotent(module = "BizCategory", expire = 30)
    public Result<Long> create(@Valid @RequestBody BizCategory entity) {
        return Result.ok(service.create(entity));
    }

    @PutMapping("/{id}")
    @RequirePermission("base:category:update")
    @AuditLog(title = "修改商品分类", module = "商品分类", businessType = "UPDATE")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody BizCategory entity) {
        entity.setId(id);
        service.update(entity);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @RequirePermission("base:category:delete")
    @AuditLog(title = "删除商品分类", module = "商品分类", businessType = "DELETE")
    public Result<Void> remove(@PathVariable Long id) {
        service.remove(id);
        return Result.ok();
    }

    @GetMapping("/export")
    @RequirePermission("base:category:export")
    @AuditLog(title = "导出商品分类", module = "商品分类", businessType = "EXPORT")
    public void export(@Valid BizCategoryQuery query, HttpServletResponse response) {
        service.export(query, response);
    }

    @PostMapping("/import")
    @RequirePermission("base:category:import")
    @AuditLog(title = "导入商品分类", module = "商品分类", businessType = "IMPORT", saveResponse = true)
    public Result<ExcelImportResult> importData(@RequestParam("file") MultipartFile file) {
        return Result.ok(service.importData(file));
    }

}

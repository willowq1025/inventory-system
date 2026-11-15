package com.enterprise.module.system.controller;

import com.enterprise.common.core.Result;
import com.enterprise.common.log.annotation.AuditLog;
import com.enterprise.common.security.annotation.RequirePermission;
import com.enterprise.module.system.dto.DeptCmd;
import com.enterprise.module.system.entity.SysDept;
import com.enterprise.module.system.service.SysDeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/system/depts")
@RequiredArgsConstructor
public class SysDeptController {

    private final SysDeptService service;

    @GetMapping
    @RequirePermission("system:dept:list")
    public Result<List<SysDept>> tree() {
        return Result.ok(service.tree());
    }

    @PostMapping
    @RequirePermission("system:dept:create")
    @AuditLog(title = "新增部门", module = "系统管理", businessType = "INSERT")
    public Result<Long> create(@RequestBody DeptCmd cmd) {
        return Result.ok(service.create(cmd));
    }

    @PutMapping("/{id}")
    @RequirePermission("system:dept:update")
    @AuditLog(title = "修改部门", module = "系统管理", businessType = "UPDATE")
    public Result<Void> update(@PathVariable Long id, @RequestBody DeptCmd cmd) {
        cmd.setId(id);
        service.update(cmd);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @RequirePermission("system:dept:delete")
    @AuditLog(title = "删除部门", module = "系统管理", businessType = "DELETE")
    public Result<Void> remove(@PathVariable Long id) {
        service.remove(id);
        return Result.ok();
    }
}

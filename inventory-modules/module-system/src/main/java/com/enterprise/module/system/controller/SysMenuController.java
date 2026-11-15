package com.enterprise.module.system.controller;

import com.enterprise.common.core.Result;
import com.enterprise.common.log.annotation.AuditLog;
import com.enterprise.common.security.annotation.RequirePermission;
import com.enterprise.module.system.dto.MenuCmd;
import com.enterprise.module.system.entity.SysMenu;
import com.enterprise.module.system.service.SysMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/system/menus")
@RequiredArgsConstructor
public class SysMenuController {

    private final SysMenuService service;

    @GetMapping
    @RequirePermission("system:menu:list")
    public Result<List<SysMenu>> tree() {
        return Result.ok(service.tree());
    }

    @GetMapping("/permissions")
    @RequirePermission("system:menu:list")
    public Result<List<SysMenu>> permissions() {
        return Result.ok(service.permissionTree());
    }

    @PostMapping
    @RequirePermission("system:menu:create")
    @AuditLog(title = "新增菜单/权限", module = "系统管理", businessType = "INSERT")
    public Result<Long> create(@RequestBody MenuCmd cmd) {
        return Result.ok(service.create(cmd));
    }

    @PutMapping("/{id}")
    @RequirePermission("system:menu:update")
    @AuditLog(title = "修改菜单/权限", module = "系统管理", businessType = "UPDATE")
    public Result<Void> update(@PathVariable Long id, @RequestBody MenuCmd cmd) {
        cmd.setId(id);
        service.update(cmd);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @RequirePermission("system:menu:delete")
    @AuditLog(title = "删除菜单/权限", module = "系统管理", businessType = "DELETE")
    public Result<Void> remove(@PathVariable Long id) {
        service.remove(id);
        return Result.ok();
    }
}

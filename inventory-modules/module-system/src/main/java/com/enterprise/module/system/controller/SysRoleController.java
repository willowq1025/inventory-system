package com.enterprise.module.system.controller;

import com.enterprise.common.core.PageResult;
import com.enterprise.common.core.Result;
import com.enterprise.common.core.validation.Create;
import com.enterprise.common.core.validation.Update;
import com.enterprise.common.log.annotation.AuditLog;
import com.enterprise.common.redis.annotation.Idempotent;
import com.enterprise.common.security.annotation.RequirePermission;
import com.enterprise.module.system.dto.RoleCmd;
import com.enterprise.module.system.dto.RoleQuery;
import com.enterprise.module.system.entity.SysRole;
import com.enterprise.module.system.service.SysRoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/system/roles")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService service;

    @GetMapping
    @RequirePermission("system:role:list")
    public Result<PageResult<SysRole>> page(@Valid RoleQuery query) {
        return Result.ok(service.page(query));
    }

    @GetMapping("/all")
    @RequirePermission("system:role:list")
    public Result<List<SysRole>> all() {
        return Result.ok(service.listAll());
    }

    @GetMapping("/{id}")
    @RequirePermission("system:role:list")
    public Result<RoleCmd> detail(@PathVariable Long id) {
        return Result.ok(service.detail(id));
    }

    @PostMapping
    @RequirePermission("system:role:create")
    @AuditLog(title = "新增角色", module = "系统管理", businessType = "INSERT", saveResponse = true)
    @Idempotent(module = "role", expire = 30)
    public Result<Long> create(@Validated(Create.class) @RequestBody RoleCmd cmd) {
        return Result.ok(service.create(cmd));
    }

    @PutMapping("/{id}")
    @RequirePermission("system:role:update")
    @AuditLog(title = "修改角色", module = "系统管理", businessType = "UPDATE")
    public Result<Void> update(@PathVariable Long id, @Validated(Update.class) @RequestBody RoleCmd cmd) {
        cmd.setId(id);
        service.update(cmd);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @RequirePermission("system:role:delete")
    @AuditLog(title = "删除角色", module = "系统管理", businessType = "DELETE")
    public Result<Void> remove(@PathVariable Long id) {
        service.remove(id);
        return Result.ok();
    }

    /** 分配菜单 / 接口权限 + 自定义数据权限部门 */
    @PutMapping("/{id}/menus")
    @RequirePermission("system:role:grant")
    @AuditLog(title = "分配角色权限", module = "系统管理", businessType = "UPDATE")
    public Result<Void> grant(@PathVariable Long id, @RequestBody Map<String, List<Long>> body) {
        service.grant(id, body.get("menuIds"), body.get("deptIds"));
        return Result.ok();
    }
}

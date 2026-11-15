package com.enterprise.module.system.controller;

import com.enterprise.common.core.PageResult;
import com.enterprise.common.core.Result;
import com.enterprise.common.core.validation.Create;
import com.enterprise.common.core.validation.Update;
import com.enterprise.common.log.annotation.AuditLog;
import com.enterprise.common.redis.annotation.Idempotent;
import com.enterprise.common.security.annotation.RequirePermission;
import com.enterprise.module.system.dto.UserCmd;
import com.enterprise.module.system.dto.UserQuery;
import com.enterprise.module.system.entity.SysUser;
import com.enterprise.module.system.service.SysUserService;
import com.enterprise.module.system.vo.UserVO;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/system/users")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService service;

    @GetMapping
    @RequirePermission("system:user:list")
    public Result<PageResult<SysUser>> page(@Valid UserQuery query) {
        return Result.ok(service.page(query));
    }

    @GetMapping("/{id}")
    @RequirePermission("system:user:list")
    public Result<UserVO> detail(@PathVariable Long id) {
        return Result.ok(service.detail(id));
    }

    @PostMapping
    @RequirePermission("system:user:create")
    @AuditLog(title = "新增用户", module = "系统管理", businessType = "INSERT", saveResponse = true)
    @Idempotent(module = "user", expire = 30)
    public Result<Long> create(@Validated(Create.class) @RequestBody UserCmd cmd) {
        return Result.ok(service.create(cmd));
    }

    @PutMapping("/{id}")
    @RequirePermission("system:user:update")
    @AuditLog(title = "修改用户", module = "系统管理", businessType = "UPDATE")
    public Result<Void> update(@PathVariable Long id, @Validated(Update.class) @RequestBody UserCmd cmd) {
        cmd.setId(id);
        service.update(cmd);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @RequirePermission("system:user:delete")
    @AuditLog(title = "删除用户", module = "系统管理", businessType = "DELETE")
    public Result<Void> remove(@PathVariable Long id) {
        service.remove(id);
        return Result.ok();
    }

    @PutMapping("/{id}/password")
    @RequirePermission("system:user:update")
    @AuditLog(title = "重置密码", module = "系统管理", businessType = "UPDATE", saveRequest = false)
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        service.resetPassword(id, body.get("password"));
        return Result.ok();
    }

    @GetMapping("/export")
    @RequirePermission("system:user:export")
    @AuditLog(title = "导出用户", module = "系统管理", businessType = "EXPORT")
    public void export(@Valid UserQuery query, HttpServletResponse response) {
        service.export(query, response);
    }
}

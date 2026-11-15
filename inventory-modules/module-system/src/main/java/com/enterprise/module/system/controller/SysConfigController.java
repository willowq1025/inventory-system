package com.enterprise.module.system.controller;

import com.enterprise.common.core.Result;
import com.enterprise.common.log.annotation.AuditLog;
import com.enterprise.common.security.annotation.RequirePermission;
import com.enterprise.module.system.entity.SysConfig;
import com.enterprise.module.system.service.SysConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/system/configs")
@RequiredArgsConstructor
public class SysConfigController {

    private final SysConfigService service;

    @GetMapping
    @RequirePermission("system:config:list")
    public Result<List<SysConfig>> list() {
        return Result.ok(service.listAll());
    }

    @PutMapping("/{id}")
    @RequirePermission("system:config:update")
    @AuditLog(title = "修改系统参数", module = "系统管理", businessType = "UPDATE")
    public Result<Void> update(@PathVariable Long id, @RequestBody Map<String, String> body) {
        service.update(id, body.get("configValue"));
        return Result.ok();
    }
}

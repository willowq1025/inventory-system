package com.enterprise.module.system.controller;

import com.enterprise.common.core.Result;
import com.enterprise.common.log.annotation.AuditLog;
import com.enterprise.common.security.annotation.RequirePermission;
import com.enterprise.module.system.entity.SysDictData;
import com.enterprise.module.system.entity.SysDictType;
import com.enterprise.module.system.service.SysDictService;
import com.enterprise.module.system.vo.DictVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** 字典接口：读走 Redis 缓存，写走事务提交后失效，保证一致性。 */
@Validated
@RestController
@RequestMapping("/api/system/dict")
@RequiredArgsConstructor
public class SysDictController {

    private final SysDictService service;

    @GetMapping("/types")
    @RequirePermission("system:dict:list")
    public Result<List<SysDictType>> types() {
        return Result.ok(service.types());
    }

    @GetMapping("/type/{dictType}")
    public Result<List<DictVO>> byType(@PathVariable String dictType) {
        return Result.ok(service.byType(dictType));
    }

    @GetMapping("/all")
    public Result<Map<String, List<DictVO>>> all() {
        return Result.ok(service.all());
    }

    @PostMapping("/data")
    @RequirePermission("system:dict:update")
    @AuditLog(title = "新增/修改字典项", module = "系统管理", businessType = "UPDATE")
    public Result<Void> saveData(@RequestBody SysDictData data) {
        service.saveData(data);
        return Result.ok();
    }

    @DeleteMapping("/data/{id}")
    @RequirePermission("system:dict:update")
    @AuditLog(title = "删除字典项", module = "系统管理", businessType = "DELETE")
    public Result<Void> removeData(@PathVariable Long id) {
        service.removeData(id);
        return Result.ok();
    }

    @PostMapping("/type")
    @RequirePermission("system:dict:update")
    public Result<Void> saveType(@RequestBody SysDictType type) {
        service.saveType(type);
        return Result.ok();
    }
}

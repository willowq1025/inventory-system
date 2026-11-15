package com.enterprise.module.system.controller;

import com.enterprise.common.core.PageResult;
import com.enterprise.common.core.Result;
import com.enterprise.common.log.annotation.AuditLog;
import com.enterprise.common.security.annotation.RequirePermission;
import com.enterprise.module.system.dto.NoticeCmd;
import com.enterprise.module.system.dto.NoticeQuery;
import com.enterprise.module.system.entity.SysNotice;
import com.enterprise.module.system.service.SysNoticeService;
import com.enterprise.module.system.vo.NoticeVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/system/notices")
@RequiredArgsConstructor
public class SysNoticeController {

    private final SysNoticeService service;

    @GetMapping
    @RequirePermission("system:notice:list")
    public Result<PageResult<SysNotice>> page(@Valid NoticeQuery query) {
        return Result.ok(service.page(query));
    }

    @GetMapping("/mine")
    public Result<List<NoticeVO>> mine() {
        return Result.ok(service.mine(service.currentUserId()));
    }

    @GetMapping("/unread-count")
    public Result<Long> unreadCount() {
        return Result.ok(service.unreadCount(service.currentUserId()));
    }

    @PostMapping("/read/{id}")
    public Result<Void> markRead(@PathVariable Long id) {
        service.markRead(id, service.currentUserId());
        return Result.ok();
    }

    @PostMapping("/read-all")
    public Result<Void> markAllRead() {
        service.markAllRead(service.currentUserId());
        return Result.ok();
    }

    @PostMapping("/publish")
    @RequirePermission("system:notice:create")
    @AuditLog(title = "发布通知", module = "消息中心", businessType = "INSERT")
    public Result<Void> publish(@RequestBody NoticeCmd cmd) {
        service.publish(cmd);
        return Result.ok();
    }
}

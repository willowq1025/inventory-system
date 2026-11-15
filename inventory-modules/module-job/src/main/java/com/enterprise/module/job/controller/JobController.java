package com.enterprise.module.job.controller;

import com.enterprise.common.core.Result;
import com.enterprise.common.security.annotation.RequirePermission;
import com.enterprise.module.job.job.*;
import com.enterprise.module.job.support.JobLeader;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/** 运维入口：查看任务清单 / 手动触发（便于验证与应急补跑）。 */
@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobLeader jobLeader;
    private final OrderTimeoutJob orderTimeoutJob;
    private final LowStockJob lowStockJob;
    private final DailyStatJob dailyStatJob;
    private final CleanupJob cleanupJob;

    @GetMapping
    public Result<Map<String, String>> list() {
        Map<String, String> jobs = new LinkedHashMap<>();
        jobs.put("order-timeout", "待审核订单超时提醒（每小时）");
        jobs.put("low-stock", "低库存预警（每 30 分钟，每日去重）");
        jobs.put("daily-stat", "每日经营统计预计算（00:10）");
        jobs.put("cleanup", "过期数据清理（03:00）");
        return Result.ok(jobs);
    }

    @PostMapping("/{name}/run")
    @RequirePermission("system:job:run")
    public Result<Boolean> run(@PathVariable String name) {
        return switch (name) {
            case "order-timeout" -> Result.ok(jobLeader.runIfLeader(name, 300, orderTimeoutJob::checkTimeoutNested));
            case "low-stock" -> Result.ok(jobLeader.runIfLeader(name, 300, lowStockJob::checkLowStockNested));
            case "daily-stat" -> Result.ok(jobLeader.runIfLeader(name, 300, dailyStatJob::computeNested));
            case "cleanup" -> Result.ok(jobLeader.runIfLeader(name, 300, cleanupJob::cleanupNested));
            default -> Result.ok(false);
        };
    }
}

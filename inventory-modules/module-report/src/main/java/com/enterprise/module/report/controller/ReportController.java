package com.enterprise.module.report.controller;

import com.enterprise.common.core.Result;
import com.enterprise.common.log.annotation.AuditLog;
import com.enterprise.common.security.annotation.RequirePermission;
import com.enterprise.module.report.dto.PssQuery;
import com.enterprise.module.report.service.ReportService;
import com.enterprise.module.report.vo.DashboardVO;
import com.enterprise.module.report.vo.PssVO;
import com.enterprise.module.report.vo.TrendPoint;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService service;

    @GetMapping("/dashboard")
    @RequirePermission("report:dashboard:view")
    public Result<DashboardVO> dashboard(@RequestParam(defaultValue = "7") int days) {
        return Result.ok(service.dashboard(days));
    }

    @GetMapping("/trend")
    @RequirePermission("report:dashboard:view")
    public Result<List<TrendPoint>> trend(@RequestParam(defaultValue = "7") int days) {
        return Result.ok(service.trend(days));
    }

    @GetMapping("/warehouse-distribution")
    @RequirePermission("report:dashboard:view")
    public Result<List<Map<String, Object>>> warehouseDistribution() {
        return Result.ok(service.warehouseDistribution());
    }

    @GetMapping("/pss")
    @RequirePermission("report:pss:list")
    public Result<List<PssVO>> pss(PssQuery query) {
        return Result.ok(service.pss(query));
    }

    @GetMapping("/pss/export")
    @RequirePermission("report:pss:export")
    @AuditLog(title = "导出进销存报表", module = "报表统计", businessType = "EXPORT")
    public void exportPss(PssQuery query, HttpServletResponse response) {
        service.exportPss(query, response);
    }
}

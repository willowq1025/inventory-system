package com.enterprise.module.inventory.controller;

import com.enterprise.common.core.PageResult;
import com.enterprise.common.core.Result;
import com.enterprise.common.excel.util.ExcelUtils;
import com.enterprise.common.log.annotation.AuditLog;
import com.enterprise.common.redis.annotation.Idempotent;
import com.enterprise.common.security.annotation.RequirePermission;
import com.enterprise.module.inventory.dto.StockChangeCmd;
import com.enterprise.module.inventory.dto.StockQuery;
import com.enterprise.module.inventory.service.StockService;
import com.enterprise.module.inventory.vo.StockVO;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/inventory/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @GetMapping
    @RequirePermission("inventory:stock:list")
    public Result<PageResult<StockVO>> page(@Valid StockQuery query) {
        return Result.ok(stockService.page(query));
    }

    @GetMapping("/summary")
    @RequirePermission("inventory:stock:list")
    public Result<Map<String, Object>> summary() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalQuantity", stockService.totalQuantity());
        data.put("lowStockCount", stockService.lowStockCount());
        return Result.ok(data);
    }

    /** 手工库存调整：幂等 + 审计 + 分布式锁 + 原子 CAS */
    @PostMapping("/adjust")
    @RequirePermission("inventory:stock:adjust")
    @AuditLog(title = "库存调整", module = "库存管理", businessType = "UPDATE", saveResponse = true)
    @Idempotent(module = "stock-adjust", expire = 60)
    public Result<Long> adjust(@Valid @RequestBody StockChangeCmd cmd) {
        return Result.ok(stockService.change(cmd));
    }

    @GetMapping("/export")
    @RequirePermission("inventory:stock:export")
    @AuditLog(title = "导出库存", module = "库存管理", businessType = "EXPORT")
    public void export(@Valid StockQuery query, HttpServletResponse response) {
        ExcelUtils.export(response, stockService.listAll(query), StockVO.class, "库存台账", "库存");
    }
}

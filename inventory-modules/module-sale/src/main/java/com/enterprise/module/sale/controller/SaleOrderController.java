package com.enterprise.module.sale.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.enterprise.common.core.PageResult;
import com.enterprise.common.core.Result;
import com.enterprise.common.log.annotation.AuditLog;
import com.enterprise.common.redis.annotation.Idempotent;
import com.enterprise.common.security.annotation.RequirePermission;
import com.enterprise.module.sale.dto.SaleOrderCmd;
import com.enterprise.module.sale.dto.SaleOrderQuery;
import com.enterprise.module.sale.entity.BizSaleOrder;
import com.enterprise.module.sale.entity.BizSaleItem;
import com.enterprise.module.sale.service.SaleOrderService;
import com.enterprise.module.sale.vo.SaleOrderVO;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/sale/orders")
@RequiredArgsConstructor
public class SaleOrderController {

    private final SaleOrderService service;

    @GetMapping
    @RequirePermission("sale:order:list")
    public Result<PageResult<BizSaleOrder>> page(@Valid SaleOrderQuery query) {
        return Result.ok(service.page(query));
    }

    @GetMapping("/{id}")
    @RequirePermission("sale:order:list")
    public Result<SaleOrderVO> detail(@PathVariable Long id) {
        return Result.ok(service.detail(id));
    }

    @PostMapping
    @RequirePermission("sale:order:create")
    @AuditLog(title = "创建销售订单", module = "销售管理", businessType = "INSERT", saveResponse = true)
    @Idempotent(module = "sale-order", expire = 60)
    public Result<Long> create(@Valid @RequestBody SaleOrderCmd cmd) {
        return Result.ok(service.create(cmd));
    }

    @PostMapping("/{id}/audit")
    @RequirePermission("sale:order:audit")
    @AuditLog(title = "审核销售订单", module = "销售管理", businessType = "AUDIT")
    @Idempotent(module = "sale-audit", key = "#id", expire = 300)
    public Result<Void> audit(@PathVariable("id") Long id, @RequestBody(required = false) Map<String, Boolean> body) {
        service.audit(id, body == null || !Boolean.FALSE.equals(body.get("pass")));
        return Result.ok();
    }

    @PostMapping("/{id}/stock-out")
    @RequirePermission("sale:order:stock-out")
    @AuditLog(title = "销售订单出库", module = "销售管理", businessType = "UPDATE")
    @Idempotent(module = "sale-stock-out", key = "#id", expire = 300)
    public Result<Void> stockOut(@PathVariable("id") Long id) {
        service.stockOut(id);
        return Result.ok();
    }

    @PostMapping("/{id}/cancel")
    @RequirePermission("sale:order:audit")
    @AuditLog(title = "取消销售订单", module = "销售管理", businessType = "UPDATE")
    public Result<Void> cancel(@PathVariable Long id) {
        service.cancel(id);
        return Result.ok();
    }

    @GetMapping("/export")
    @RequirePermission("sale:order:export")
    @AuditLog(title = "导出销售订单", module = "销售管理", businessType = "EXPORT")
    public void export(@Valid SaleOrderQuery query, HttpServletResponse response) {
        service.export(query, response);
    }
}

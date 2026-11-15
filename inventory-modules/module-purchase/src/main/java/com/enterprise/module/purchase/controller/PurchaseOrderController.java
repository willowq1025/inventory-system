package com.enterprise.module.purchase.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.enterprise.common.core.PageResult;
import com.enterprise.common.core.Result;
import com.enterprise.common.log.annotation.AuditLog;
import com.enterprise.common.redis.annotation.Idempotent;
import com.enterprise.common.security.annotation.RequirePermission;
import com.enterprise.module.purchase.dto.PurchaseOrderCmd;
import com.enterprise.module.purchase.dto.PurchaseOrderQuery;
import com.enterprise.module.purchase.entity.BizPurchaseOrder;
import com.enterprise.module.purchase.entity.BizPurchaseItem;
import com.enterprise.module.purchase.service.PurchaseOrderService;
import com.enterprise.module.purchase.vo.PurchaseOrderVO;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/purchase/orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService service;

    @GetMapping
    @RequirePermission("purchase:order:list")
    public Result<PageResult<BizPurchaseOrder>> page(@Valid PurchaseOrderQuery query) {
        return Result.ok(service.page(query));
    }

    @GetMapping("/{id}")
    @RequirePermission("purchase:order:list")
    public Result<PurchaseOrderVO> detail(@PathVariable Long id) {
        return Result.ok(service.detail(id));
    }

    @PostMapping
    @RequirePermission("purchase:order:create")
    @AuditLog(title = "创建采购订单", module = "采购管理", businessType = "INSERT", saveResponse = true)
    @Idempotent(module = "purchase-order", expire = 60)
    public Result<Long> create(@Valid @RequestBody PurchaseOrderCmd cmd) {
        return Result.ok(service.create(cmd));
    }

    @PostMapping("/{id}/audit")
    @RequirePermission("purchase:order:audit")
    @AuditLog(title = "审核采购订单", module = "采购管理", businessType = "AUDIT")
    @Idempotent(module = "purchase-audit", key = "#id", expire = 300)
    public Result<Void> audit(@PathVariable("id") Long id, @RequestBody(required = false) Map<String, Boolean> body) {
        service.audit(id, body == null || !Boolean.FALSE.equals(body.get("pass")));
        return Result.ok();
    }

    @PostMapping("/{id}/stock-in")
    @RequirePermission("purchase:order:stock-in")
    @AuditLog(title = "采购订单入库", module = "采购管理", businessType = "UPDATE")
    @Idempotent(module = "purchase-stock-in", key = "#id", expire = 300)
    public Result<Void> stockIn(@PathVariable("id") Long id) {
        service.stockIn(id);
        return Result.ok();
    }

    @PostMapping("/{id}/cancel")
    @RequirePermission("purchase:order:audit")
    @AuditLog(title = "取消采购订单", module = "采购管理", businessType = "UPDATE")
    public Result<Void> cancel(@PathVariable Long id) {
        service.cancel(id);
        return Result.ok();
    }

    @GetMapping("/export")
    @RequirePermission("purchase:order:export")
    @AuditLog(title = "导出采购订单", module = "采购管理", businessType = "EXPORT")
    public void export(@Valid PurchaseOrderQuery query, HttpServletResponse response) {
        service.export(query, response);
    }
}

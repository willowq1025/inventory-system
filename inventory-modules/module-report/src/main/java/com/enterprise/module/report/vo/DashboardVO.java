package com.enterprise.module.report.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** 经营看板：核心指标 + 近 N 日趋势 + 仓库库存分布。 */
@Data
@Builder
public class DashboardVO {

    private BigDecimal todayPurchase;
    private BigDecimal todaySale;
    private BigDecimal monthPurchase;
    private BigDecimal monthSale;
    private long todayPurchaseCount;
    private long todaySaleCount;
    private long pendingPurchase;
    private long pendingSale;
    private long stockTotal;
    private int lowStockCount;
    private List<TrendPoint> trend;
    private List<Map<String, Object>> warehouseDistribution;
}

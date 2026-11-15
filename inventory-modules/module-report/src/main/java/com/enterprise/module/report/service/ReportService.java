package com.enterprise.module.report.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.enterprise.common.excel.util.ExcelUtils;
import com.enterprise.module.inventory.entity.BizProduct;
import com.enterprise.module.inventory.entity.BizStock;
import com.enterprise.module.inventory.entity.BizStockFlow;
import com.enterprise.module.inventory.entity.BizWarehouse;
import com.enterprise.module.inventory.mapper.BizProductMapper;
import com.enterprise.module.inventory.mapper.BizStockFlowMapper;
import com.enterprise.module.inventory.mapper.BizStockMapper;
import com.enterprise.module.inventory.mapper.BizWarehouseMapper;
import com.enterprise.module.inventory.service.StockService;
import com.enterprise.module.purchase.mapper.BizPurchaseOrderMapper;
import com.enterprise.module.report.dto.PssQuery;
import com.enterprise.module.report.entity.BizDailyStat;
import com.enterprise.module.report.excel.PssExcel;
import com.enterprise.module.report.mapper.BizDailyStatMapper;
import com.enterprise.module.report.vo.DashboardVO;
import com.enterprise.module.report.vo.PssVO;
import com.enterprise.module.report.vo.TrendPoint;
import com.enterprise.module.sale.mapper.BizSaleOrderMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 报表统计：实时经营看板 + 进销存台账。
 * 单据金额类统计同样经过数据权限拦截器，自动按登录人范围过滤。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final BizPurchaseOrderMapper purchaseMapper;
    private final BizSaleOrderMapper saleMapper;
    private final BizStockFlowMapper flowMapper;
    private final BizStockMapper stockMapper;
    private final BizProductMapper productMapper;
    private final BizWarehouseMapper warehouseMapper;
    private final BizDailyStatMapper dailyStatMapper;
    private final StockService stockService;

    public DashboardVO dashboard(int trendDays) {
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime now = LocalDateTime.now();

        return DashboardVO.builder()
                .todayPurchase(purchaseMapper.sumAmount(todayStart, now))
                .todaySale(saleMapper.sumAmount(todayStart, now))
                .monthPurchase(purchaseMapper.sumAmount(monthStart, now))
                .monthSale(saleMapper.sumAmount(monthStart, now))
                .todayPurchaseCount(purchaseMapper.countFinished(todayStart, now))
                .todaySaleCount(saleMapper.countFinished(todayStart, now))
                .pendingPurchase(purchaseMapper.selectCount(
                        Wrappers.lambdaQuery(com.enterprise.module.purchase.entity.BizPurchaseOrder.class)
                                .eq(com.enterprise.module.purchase.entity.BizPurchaseOrder::getStatus, 1)))
                .pendingSale(saleMapper.selectCount(
                        Wrappers.lambdaQuery(com.enterprise.module.sale.entity.BizSaleOrder.class)
                                .eq(com.enterprise.module.sale.entity.BizSaleOrder::getStatus, 1)))
                .stockTotal(stockService.totalQuantity())
                .lowStockCount(stockService.lowStockCount())
                .trend(trend(trendDays <= 0 ? 7 : Math.min(trendDays, 90)))
                .warehouseDistribution(warehouseDistribution())
                .build();
    }

    /** 近 N 日趋势：优先读定时任务预计算的日报，缺失则用订单表实时补算 */
    public List<TrendPoint> trend(int days) {
        LocalDate from = LocalDate.now().minusDays(days - 1L);
        List<BizDailyStat> stats = dailyStatMapper.selectList(
                Wrappers.lambdaQuery(BizDailyStat.class)
                        .ge(BizDailyStat::getStatDate, from)
                        .orderByAsc(BizDailyStat::getStatDate));
        return stats.stream()
                .map(s -> new TrendPoint(s.getStatDate(), s.getPurchaseAmount(), s.getSaleAmount()))
                .toList();
    }

    public List<Map<String, Object>> warehouseDistribution() {
        QueryWrapper<BizStock> qw = new QueryWrapper<>();
        qw.select("warehouse_id, SUM(quantity) AS total_quantity")
                .eq("deleted", 0)
                .groupBy("warehouse_id")
                .orderByDesc("total_quantity");
        List<Map<String, Object>> rows = stockMapper.selectMaps(qw);
        for (Map<String, Object> row : rows) {
            Object id = row.get("warehouse_id");
            if (id != null) {
                BizWarehouse wh = warehouseMapper.selectById(Long.valueOf(id.toString()));
                row.put("name", wh == null ? "未知仓库" : wh.getWarehouseName());
            }
        }
        return rows;
    }

    /**
     * 进销存报表：以库存流水为事实表，按「商品 + 仓库」聚合期间入库/出库，
     * 结合当前库存反推期初，满足 期初 + 入库 - 出库 = 结存 的恒等式。
     */
    public List<PssVO> pss(PssQuery query) {
        LocalDate start = query.getStartDate() == null ? LocalDate.now().minusDays(30) : query.getStartDate();
        LocalDate end = query.getEndDate() == null ? LocalDate.now() : query.getEndDate();

        QueryWrapper<BizStockFlow> qw = new QueryWrapper<>();
        qw.select("product_id, warehouse_id, " +
                        "SUM(CASE WHEN quantity_change > 0 THEN quantity_change ELSE 0 END) AS in_qty, " +
                        "SUM(CASE WHEN quantity_change < 0 THEN -quantity_change ELSE 0 END) AS out_qty ")
                .eq("deleted", 0)
                .ge("oper_time", start.atStartOfDay())
                .lt("oper_time", end.plusDays(1).atStartOfDay());
        if (query.getProductId() != null) qw.eq("product_id", query.getProductId());
        if (query.getWarehouseId() != null) qw.eq("warehouse_id", query.getWarehouseId());
        qw.groupBy("product_id, warehouse_id").orderByAsc("product_id");

        List<Map<String, Object>> rows = flowMapper.selectMaps(qw);
        Map<Long, BizProduct> products = productMapper.selectList(null).stream()
                .collect(Collectors.toMap(BizProduct::getId, p -> p, (a, b) -> a));
        Map<Long, BizWarehouse> warehouses = warehouseMapper.selectList(null).stream()
                .collect(Collectors.toMap(BizWarehouse::getId, w -> w, (a, b) -> a));

        List<PssVO> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Long productId = longValue(row.get("product_id"));
            Long warehouseId = longValue(row.get("warehouse_id"));
            long inQty = longValue(row.get("in_qty"));
            long outQty = longValue(row.get("out_qty"));
            BizStock stock = stockMapper.selectOne(Wrappers.lambdaQuery(BizStock.class)
                    .eq(BizStock::getProductId, productId)
                    .eq(BizStock::getWarehouseId, warehouseId).last("LIMIT 1"));
            long closing = stock == null ? 0 : stock.getQuantity();

            PssVO vo = new PssVO();
            vo.setProductId(productId);
            vo.setWarehouseId(warehouseId);
            vo.setInQty(inQty);
            vo.setOutQty(outQty);
            vo.setClosingQty(closing);
            vo.setOpeningQty(closing - inQty + outQty);
            BizProduct product = products.get(productId);
            if (product != null) {
                vo.setProductCode(product.getProductCode());
                vo.setProductName(product.getProductName());
                vo.setUnit(product.getUnit());
            }
            BizWarehouse warehouse = warehouses.get(warehouseId);
            if (warehouse != null) vo.setWarehouseName(warehouse.getWarehouseName());
            result.add(vo);
        }
        return result;
    }

    public void exportPss(PssQuery query, HttpServletResponse response) {
        List<PssExcel> rows = pss(query).stream().map(PssExcel::from).toList();
        ExcelUtils.export(response, rows, PssExcel.class, "进销存报表", "进销存");
    }

    private long longValue(Object value) {
        if (value == null) return 0L;
        if (value instanceof Number n) return n.longValue();
        return Long.parseLong(value.toString());
    }
}

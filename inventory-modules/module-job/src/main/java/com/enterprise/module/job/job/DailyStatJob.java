package com.enterprise.module.job.job;

import com.enterprise.module.inventory.mapper.BizStockMapper;
import com.enterprise.module.job.support.JobLeader;
import com.enterprise.module.purchase.mapper.BizPurchaseOrderMapper;
import com.enterprise.module.report.entity.BizDailyStat;
import com.enterprise.module.report.mapper.BizDailyStatMapper;
import com.enterprise.module.sale.mapper.BizSaleOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 每日经营统计预计算：把昨日采购/销售/库存指标落库，供趋势图快速读取。 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DailyStatJob {

    private final JobLeader jobLeader;
    private final BizPurchaseOrderMapper purchaseMapper;
    private final BizSaleOrderMapper saleMapper;
    private final BizStockMapper stockMapper;
    private final BizDailyStatMapper dailyStatMapper;

    /** 跳过集群选主，供运维手动触发 */
    public void computeNested() {
        computeStat(LocalDate.now().minusDays(1));
    }

    @Scheduled(cron = "${inventory.job.daily-stat-cron:0 10 0 * * ?}")
    public void compute() {
        jobLeader.runIfLeader("daily-stat", 300, () -> computeStat(LocalDate.now().minusDays(1)));
    }

    private void computeStat(LocalDate day) {
            LocalDateTime start = day.atStartOfDay();
            LocalDateTime end = day.plusDays(1).atStartOfDay();
            BizDailyStat stat = dailyStatMapper.selectByDate(day);
            if (stat == null) {
                stat = new BizDailyStat();
                stat.setStatDate(day);
            }
            stat.setPurchaseAmount(purchaseMapper.sumAmount(start, end));
            stat.setSaleAmount(saleMapper.sumAmount(start, end));
            stat.setPurchaseCount(purchaseMapper.countFinished(start, end));
            stat.setSaleCount(saleMapper.countFinished(start, end));
            stat.setStockTotal(stockMapper.totalQuantity());
            stat.setLowStockCount(stockMapper.countLowStock());
        if (stat.getId() == null) dailyStatMapper.insert(stat);
        else dailyStatMapper.updateById(stat);
    }
}

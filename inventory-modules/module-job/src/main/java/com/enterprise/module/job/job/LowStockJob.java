package com.enterprise.module.job.job;

import com.enterprise.common.notify.service.NotificationPublisher;
import com.enterprise.module.inventory.dto.StockQuery;
import com.enterprise.module.inventory.service.StockService;
import com.enterprise.module.inventory.vo.StockVO;
import com.enterprise.module.job.support.JobLeader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.util.stream.Collectors;

/** 低库存预警：每天最多发送一次全体预警，避免重复骚扰。 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LowStockJob {

    private final JobLeader jobLeader;
    private final StockService stockService;
    private final NotificationPublisher notificationPublisher;
    private final StringRedisTemplate redis;

    /** 跳过集群选主，供运维手动触发（忽略当日去重） */
    public void checkLowStockNested() {
        StockQuery query = new StockQuery();
        query.setLowStockOnly(true);
        query.setPageSize(500);
        var lowStock = stockService.page(query).getRecords();
        if (!lowStock.isEmpty()) {
            notificationPublisher.warnAll("低库存预警(手动)", "共 " + lowStock.size() + " 个品项低于安全库存");
        }
    }

    @Scheduled(cron = "${inventory.job.low-stock-cron:0 */30 * * * ?}")
    public void checkLowStock() {
        jobLeader.runIfLeader("low-stock", 300, () -> {
            String dedupeKey = "inv:job:low-stock:" + LocalDate.now();
            if (!Boolean.TRUE.equals(redis.opsForValue()
                    .setIfAbsent(dedupeKey, "1", Duration.ofDays(1)))) return;

            StockQuery query = new StockQuery();
            query.setLowStockOnly(true);
            query.setPageSize(500);
            var lowStock = stockService.page(query).getRecords();
            if (lowStock.isEmpty()) return;

            String detail = lowStock.stream().limit(20)
                    .map(v -> v.getProductName() + "(" + v.getWarehouseName() + ") 可用 " + v.getAvailableQuantity())
                    .collect(Collectors.joining("\n"));
            notificationPublisher.warnAll("低库存预警",
                    "共有 " + lowStock.size() + " 个品项低于安全库存：\n" + detail);
        });
    }
}

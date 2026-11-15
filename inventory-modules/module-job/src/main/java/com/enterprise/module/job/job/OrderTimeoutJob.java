package com.enterprise.module.job.job;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.enterprise.common.notify.service.NotificationPublisher;
import com.enterprise.module.job.support.JobLeader;
import com.enterprise.module.purchase.entity.BizPurchaseOrder;
import com.enterprise.module.purchase.mapper.BizPurchaseOrderMapper;
import com.enterprise.module.sale.entity.BizSaleOrder;
import com.enterprise.module.sale.mapper.BizSaleOrderMapper;
import com.enterprise.module.system.service.SysConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/** 超时提醒：待审核订单超过阈值小时数未处理 → 站内信通知创建人，并打标避免重复提醒。 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutJob {

    private final JobLeader jobLeader;
    private final BizPurchaseOrderMapper purchaseMapper;
    private final BizSaleOrderMapper saleMapper;
    private final SysConfigService configService;
    private final NotificationPublisher notificationPublisher;

    @Scheduled(cron = "${inventory.job.order-timeout-cron:0 0 * * * ?}")
    public void checkTimeout() {
        jobLeader.runIfLeader("order-timeout", 600, () -> {
            int hours = configService.getInt("order.audit.timeout.hours", 24);
            LocalDateTime deadline = LocalDateTime.now().minusHours(hours);
            notifyPurchase(deadline);
            notifySale(deadline);
        });
    }

    /** 跳过集群选主，供运维手动触发 */
    public void checkTimeoutNested() {
        int hours = configService.getInt("order.audit.timeout.hours", 24);
        LocalDateTime deadline = LocalDateTime.now().minusHours(hours);
        notifyPurchase(deadline);
        notifySale(deadline);
    }

    private void notifyPurchase(LocalDateTime deadline) {
        List<BizPurchaseOrder> orders = purchaseMapper.selectList(
                Wrappers.lambdaQuery(BizPurchaseOrder.class)
                        .eq(BizPurchaseOrder::getStatus, 1)
                        .eq(BizPurchaseOrder::getTimeoutNotified, 0)
                        .lt(BizPurchaseOrder::getCreateTime, deadline));
        for (BizPurchaseOrder order : orders) {
            if (order.getCreateBy() != null) {
                notificationPublisher.notifyUsers(
                        "采购订单审核超时",
                        "采购单 " + order.getOrderNo() + " 已超过 " + "审核时限，请尽快处理。",
                        2, List.of(order.getCreateBy()));
            }
            BizPurchaseOrder update = new BizPurchaseOrder();
            update.setId(order.getId());
            update.setTimeoutNotified(1);
            purchaseMapper.updateById(update);
        }
    }

    private void notifySale(LocalDateTime deadline) {
        List<BizSaleOrder> orders = saleMapper.selectList(
                Wrappers.lambdaQuery(BizSaleOrder.class)
                        .eq(BizSaleOrder::getStatus, 1)
                        .eq(BizSaleOrder::getTimeoutNotified, 0)
                        .lt(BizSaleOrder::getCreateTime, deadline));
        for (BizSaleOrder order : orders) {
            if (order.getCreateBy() != null) {
                notificationPublisher.notifyUsers(
                        "销售订单审核超时",
                        "销售单 " + order.getOrderNo() + " 已超过审核时限，请尽快处理。",
                        2, List.of(order.getCreateBy()));
            }
            BizSaleOrder update = new BizSaleOrder();
            update.setId(order.getId());
            update.setTimeoutNotified(1);
            saleMapper.updateById(update);
        }
    }
}

package com.enterprise.module.purchase.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enterprise.common.core.BizException;
import com.enterprise.common.core.CacheKey;
import com.enterprise.common.core.PageResult;
import com.enterprise.common.core.ResultCode;
import com.enterprise.common.excel.util.ExcelUtils;
import com.enterprise.common.redis.service.DistributedLockTemplate;
import com.enterprise.common.security.util.SecurityUtils;
import com.enterprise.module.inventory.dto.StockChangeCmd;
import com.enterprise.module.inventory.service.OrderNoGenerator;
import com.enterprise.module.inventory.service.StockService;
import com.enterprise.module.purchase.dto.PurchaseOrderCmd;
import com.enterprise.module.purchase.dto.PurchaseOrderQuery;
import com.enterprise.module.purchase.entity.BizPurchaseOrder;
import com.enterprise.module.purchase.entity.BizPurchaseItem;
import com.enterprise.module.purchase.mapper.BizPurchaseOrderMapper;
import com.enterprise.module.purchase.mapper.BizPurchaseItemMapper;
import com.enterprise.module.purchase.vo.PurchaseOrderVO;
import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 采购订单：草稿 → 待审核 → 已审核 → 已入库 的状态机。
 *
 * <p>并发与幂等：
 * <ul>
 *   <li>状态流转使用带 status 条件的 CAS UPDATE，杜绝并发重复审核/重复入库；</li>
 *   <li>入库在「单据级 Redisson 锁 + 编程式事务」内完成，
 *       并复用 StockService.changeInTransaction 的原子条件更新，库存绝不为负；</li>
 *   <li>Controller 上的 @Idempotent 拦截前端连点与网络重试；</li>
 *   <li>数据权限由 MyBatis-Plus 拦截器按 create_by / create_dept 自动施加。</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseOrderService extends ServiceImpl<BizPurchaseOrderMapper, BizPurchaseOrder> {

    private final BizPurchaseItemMapper itemMapper;
    private final StockService stockService;
    private final OrderNoGenerator orderNoGenerator;
    private final DistributedLockTemplate lockTemplate;
    private final TransactionTemplate transactionTemplate;

    public PageResult<BizPurchaseOrder> page(PurchaseOrderQuery q) {
        var w = Wrappers.lambdaQuery(BizPurchaseOrder.class)
                .like(StrUtil.isNotBlank(q.getOrderNo()), BizPurchaseOrder::getOrderNo, q.getOrderNo())
                .eq(q.getSupplierId() != null, BizPurchaseOrder::getSupplierId, q.getSupplierId())
                .eq(q.getWarehouseId() != null, BizPurchaseOrder::getWarehouseId, q.getWarehouseId())
                .eq(q.getStatus() != null, BizPurchaseOrder::getStatus, q.getStatus())
                .ge(q.getStartTime() != null, BizPurchaseOrder::getCreateTime, q.getStartTime())
                .le(q.getEndTime() != null, BizPurchaseOrder::getCreateTime, q.getEndTime());
        String orderBy = q.safeOrderBy();
        if (orderBy == null) w.orderByDesc(BizPurchaseOrder::getCreateTime);
        else w.last("ORDER BY " + orderBy + (q.isAsc() ? " ASC" : " DESC"));
        var p = baseMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()), w);
        return PageResult.of(p.getTotal(), p.getRecords(), p.getCurrent(), p.getSize());
    }

    public PurchaseOrderVO detail(Long id) {
        BizPurchaseOrder order = getById(id);
        if (order == null) throw new BizException(ResultCode.DATA_NOT_FOUND, "采购订单不存在");
        PurchaseOrderVO vo = new PurchaseOrderVO();
        org.springframework.beans.BeanUtils.copyProperties(order, vo);
        vo.setItems(items(id));
        return vo;
    }

    public List<BizPurchaseItem> items(Long orderId) {
        return itemMapper.selectList(Wrappers.lambdaQuery(BizPurchaseItem.class)
                .eq(BizPurchaseItem::getOrderId, orderId));
    }

    @Transactional(rollbackFor = Exception.class)
    public Long create(PurchaseOrderCmd cmd) {
        BizPurchaseOrder order = new BizPurchaseOrder();
        order.setOrderNo(orderNoGenerator.next("CG"));
        order.setSupplierId(cmd.getSupplierId());
        order.setWarehouseId(cmd.getWarehouseId());
        order.setStatus(1);   // 待审核
        order.setExpectDate(cmd.getExpectDate());
        order.setRemark(cmd.getRemark());

        BigDecimal total = BigDecimal.ZERO;
        for (PurchaseOrderCmd.Item item : cmd.getItems()) {
            total = total.add(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        order.setTotalAmount(total);
        save(order);

        for (PurchaseOrderCmd.Item item : cmd.getItems()) {
            BizPurchaseItem entity = new BizPurchaseItem();
            entity.setOrderId(order.getId());
            entity.setProductId(item.getProductId());
            entity.setQuantity(item.getQuantity());
            entity.setUnitPrice(item.getUnitPrice());
            entity.setAmount(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            itemMapper.insert(entity);
        }
        return order.getId();
    }

    /** 审核：状态 CAS，防止并发重复审核 */
    @Transactional(rollbackFor = Exception.class)
    public void audit(Long id, boolean pass) {
        boolean updated = update(Wrappers.lambdaUpdate(BizPurchaseOrder.class)
                .eq(BizPurchaseOrder::getId, id)
                .eq(BizPurchaseOrder::getStatus, 1)
                .set(BizPurchaseOrder::getStatus, pass ? 2 : 5)
                .set(BizPurchaseOrder::getAuditBy, SecurityUtils.userId())
                .set(BizPurchaseOrder::getAuditTime, LocalDateTime.now()));
        if (!updated) throw new BizException(ResultCode.ORDER_STATUS_ILLEGAL, "订单不是待审核状态或已被处理");
    }

    /** 入库：单据锁 + 编程式事务 + 库存原子扣减，全流程幂等 */
    public void stockIn(Long orderId) {
        lockTemplate.execute(CacheKey.LOCK_ORDER + "purchase:" + orderId, 5, -1, () -> {
            transactionTemplate.executeWithoutResult(status -> {
                BizPurchaseOrder order = getById(orderId);
                if (order == null) throw new BizException(ResultCode.DATA_NOT_FOUND, "采购订单不存在");
                if (order.getStatus() != 2) {
                    throw new BizException(ResultCode.ORDER_STATUS_ILLEGAL, "仅已审核订单可执行入库，请勿重复操作");
                }
                for (BizPurchaseItem item : items(orderId)) {
                    StockChangeCmd change = StockChangeCmd.of(
                            item.getProductId(), order.getWarehouseId(),
                            item.getQuantity(),
                            "PURCHASE_IN", order.getOrderNo(),
                            "采购订单入库: " + order.getOrderNo());
                    stockService.changeInTransaction(change);

                    BizPurchaseItem itemUpdate = new BizPurchaseItem();
                    itemUpdate.setId(item.getId());
                    itemUpdate.setReceivedQuantity(item.getQuantity());
                    itemMapper.updateById(itemUpdate);
                }
                BizPurchaseOrder orderUpdate = new BizPurchaseOrder();
                orderUpdate.setId(orderId);
                orderUpdate.setStatus(3);
                updateById(orderUpdate);
                log.info("[order] 采购订单入库完成 orderNo={}", order.getOrderNo());
            });
            return null;
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id) {
        BizPurchaseOrder order = getById(id);
        if (order == null) throw new BizException(ResultCode.DATA_NOT_FOUND, "采购订单不存在");
        if (order.getStatus() != null && order.getStatus() >= 3) {
            throw new BizException(ResultCode.ORDER_STATUS_ILLEGAL, "已入库订单不可取消");
        }
        BizPurchaseOrder update = new BizPurchaseOrder();
        update.setId(id);
        update.setStatus(4);
        updateById(update);
    }

    public void export(PurchaseOrderQuery q, HttpServletResponse response) {
        q.setPageSize(500);
        ExcelUtils.export(response, page(q).getRecords(), BizPurchaseOrder.class,
                "采购订单列表", "采购订单");
    }
}

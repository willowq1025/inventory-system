package com.enterprise.module.sale.service;

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
import com.enterprise.module.sale.dto.SaleOrderCmd;
import com.enterprise.module.sale.dto.SaleOrderQuery;
import com.enterprise.module.sale.entity.BizSaleOrder;
import com.enterprise.module.sale.entity.BizSaleItem;
import com.enterprise.module.sale.mapper.BizSaleOrderMapper;
import com.enterprise.module.sale.mapper.BizSaleItemMapper;
import com.enterprise.module.sale.vo.SaleOrderVO;
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
 * 销售订单：草稿 → 待审核 → 已审核 → 已出库 的状态机。
 *
 * <p>并发与幂等：
 * <ul>
 *   <li>状态流转使用带 status 条件的 CAS UPDATE，杜绝并发重复审核/重复出库；</li>
 *   <li>出库在「单据级 Redisson 锁 + 编程式事务」内完成，
 *       并复用 StockService.changeInTransaction 的原子条件更新，库存绝不为负；</li>
 *   <li>Controller 上的 @Idempotent 拦截前端连点与网络重试；</li>
 *   <li>数据权限由 MyBatis-Plus 拦截器按 create_by / create_dept 自动施加。</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SaleOrderService extends ServiceImpl<BizSaleOrderMapper, BizSaleOrder> {

    private final BizSaleItemMapper itemMapper;
    private final StockService stockService;
    private final OrderNoGenerator orderNoGenerator;
    private final DistributedLockTemplate lockTemplate;
    private final TransactionTemplate transactionTemplate;

    public PageResult<BizSaleOrder> page(SaleOrderQuery q) {
        var w = Wrappers.lambdaQuery(BizSaleOrder.class)
                .like(StrUtil.isNotBlank(q.getOrderNo()), BizSaleOrder::getOrderNo, q.getOrderNo())
                .eq(q.getCustomerId() != null, BizSaleOrder::getCustomerId, q.getCustomerId())
                .eq(q.getWarehouseId() != null, BizSaleOrder::getWarehouseId, q.getWarehouseId())
                .eq(q.getStatus() != null, BizSaleOrder::getStatus, q.getStatus())
                .ge(q.getStartTime() != null, BizSaleOrder::getCreateTime, q.getStartTime())
                .le(q.getEndTime() != null, BizSaleOrder::getCreateTime, q.getEndTime());
        String orderBy = q.safeOrderBy();
        if (orderBy == null) w.orderByDesc(BizSaleOrder::getCreateTime);
        else w.last("ORDER BY " + orderBy + (q.isAsc() ? " ASC" : " DESC"));
        var p = baseMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()), w);
        return PageResult.of(p.getTotal(), p.getRecords(), p.getCurrent(), p.getSize());
    }

    public SaleOrderVO detail(Long id) {
        BizSaleOrder order = getById(id);
        if (order == null) throw new BizException(ResultCode.DATA_NOT_FOUND, "销售订单不存在");
        SaleOrderVO vo = new SaleOrderVO();
        org.springframework.beans.BeanUtils.copyProperties(order, vo);
        vo.setItems(items(id));
        return vo;
    }

    public List<BizSaleItem> items(Long orderId) {
        return itemMapper.selectList(Wrappers.lambdaQuery(BizSaleItem.class)
                .eq(BizSaleItem::getOrderId, orderId));
    }

    @Transactional(rollbackFor = Exception.class)
    public Long create(SaleOrderCmd cmd) {
        BizSaleOrder order = new BizSaleOrder();
        order.setOrderNo(orderNoGenerator.next("XS"));
        order.setCustomerId(cmd.getCustomerId());
        order.setWarehouseId(cmd.getWarehouseId());
        order.setStatus(1);   // 待审核
        order.setExpectDate(cmd.getExpectDate());
        order.setRemark(cmd.getRemark());

        BigDecimal total = BigDecimal.ZERO;
        for (SaleOrderCmd.Item item : cmd.getItems()) {
            total = total.add(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        order.setTotalAmount(total);
        save(order);

        for (SaleOrderCmd.Item item : cmd.getItems()) {
            BizSaleItem entity = new BizSaleItem();
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
        boolean updated = update(Wrappers.lambdaUpdate(BizSaleOrder.class)
                .eq(BizSaleOrder::getId, id)
                .eq(BizSaleOrder::getStatus, 1)
                .set(BizSaleOrder::getStatus, pass ? 2 : 5)
                .set(BizSaleOrder::getAuditBy, SecurityUtils.userId())
                .set(BizSaleOrder::getAuditTime, LocalDateTime.now()));
        if (!updated) throw new BizException(ResultCode.ORDER_STATUS_ILLEGAL, "订单不是待审核状态或已被处理");
    }

    /** 出库：单据锁 + 编程式事务 + 库存原子扣减，全流程幂等 */
    public void stockOut(Long orderId) {
        lockTemplate.execute(CacheKey.LOCK_ORDER + "sale:" + orderId, 5, -1, () -> {
            transactionTemplate.executeWithoutResult(status -> {
                BizSaleOrder order = getById(orderId);
                if (order == null) throw new BizException(ResultCode.DATA_NOT_FOUND, "销售订单不存在");
                if (order.getStatus() != 2) {
                    throw new BizException(ResultCode.ORDER_STATUS_ILLEGAL, "仅已审核订单可执行出库，请勿重复操作");
                }
                for (BizSaleItem item : items(orderId)) {
                    StockChangeCmd change = StockChangeCmd.of(
                            item.getProductId(), order.getWarehouseId(),
                            -item.getQuantity(),
                            "SALE_OUT", order.getOrderNo(),
                            "销售订单出库: " + order.getOrderNo());
                    stockService.changeInTransaction(change);

                    BizSaleItem itemUpdate = new BizSaleItem();
                    itemUpdate.setId(item.getId());
                    itemUpdate.setDeliveredQuantity(item.getQuantity());
                    itemMapper.updateById(itemUpdate);
                }
                BizSaleOrder orderUpdate = new BizSaleOrder();
                orderUpdate.setId(orderId);
                orderUpdate.setStatus(3);
                updateById(orderUpdate);
                log.info("[order] 销售订单出库完成 orderNo={}", order.getOrderNo());
            });
            return null;
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id) {
        BizSaleOrder order = getById(id);
        if (order == null) throw new BizException(ResultCode.DATA_NOT_FOUND, "销售订单不存在");
        if (order.getStatus() != null && order.getStatus() >= 3) {
            throw new BizException(ResultCode.ORDER_STATUS_ILLEGAL, "已出库订单不可取消");
        }
        BizSaleOrder update = new BizSaleOrder();
        update.setId(id);
        update.setStatus(4);
        updateById(update);
    }

    public void export(SaleOrderQuery q, HttpServletResponse response) {
        q.setPageSize(500);
        ExcelUtils.export(response, page(q).getRecords(), BizSaleOrder.class,
                "销售订单列表", "销售订单");
    }
}

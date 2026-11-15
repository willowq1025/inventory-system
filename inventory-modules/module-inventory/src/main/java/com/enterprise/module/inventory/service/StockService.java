package com.enterprise.module.inventory.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enterprise.common.core.BizException;
import com.enterprise.common.core.CacheKey;
import com.enterprise.common.core.PageResult;
import com.enterprise.common.core.ResultCode;
import com.enterprise.common.redis.service.DistributedLockTemplate;
import com.enterprise.common.security.util.SecurityUtils;
import com.enterprise.module.inventory.dto.StockChangeCmd;
import com.enterprise.module.inventory.dto.StockQuery;
import com.enterprise.module.inventory.entity.BizStock;
import com.enterprise.module.inventory.entity.BizStockFlow;
import com.enterprise.module.inventory.mapper.BizStockFlowMapper;
import com.enterprise.module.inventory.mapper.BizStockMapper;
import com.enterprise.module.inventory.vo.StockVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;

/**
 * 库存核心服务 —— 并发安全的唯一扣减入口。
 *
 * <p>三层防护：
 * <ol>
 *   <li><b>幂等</b>：Controller 上的 @Idempotent（Redis SETNX + 响应快照回放）拦截重复提交；</li>
 *   <li><b>分布式锁</b>：按 productId + warehouseId 加 Redisson 可重入锁，
 *       缩小同一 SKU 的并发窗口、避免多 SKU 死锁；</li>
 *   <li><b>数据库原子 CAS</b>：单条 UPDATE ... WHERE available_quantity + delta >= 0，
 *       由 MySQL 行锁保证绝对不超卖（这是最后且最关键的一道防线）。</li>
 * </ol>
 *
 * <p>事务顺序：编程式 TransactionTemplate 放在分布式锁【内部】，
 * 保证「先加锁 → 再开事务 → 提交 → 释放锁」，避免声明式事务切面导致的
 * 「锁已释放但事务未提交」脏读窗口。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockService extends ServiceImpl<BizStockMapper, BizStock> {

    private final BizStockFlowMapper flowMapper;
    private final DistributedLockTemplate lockTemplate;
    private final TransactionTemplate transactionTemplate;

    /** 独立调用：加锁 + 事务 + 原子扣减 + 流水 */
    public long change(StockChangeCmd cmd) {
        validate(cmd);
        String lockKey = lockKey(cmd.getProductId(), cmd.getWarehouseId());
        Long after = lockTemplate.execute(lockKey, 5, -1, () ->
                transactionTemplate.execute(status -> doChange(cmd)));
        return after == null ? 0L : after;
    }

    /**
     * 供订单/盘点等上层在【其自身持有锁与事务】时复用，避免嵌套事务。
     * 调用方必须保证：外层已持有覆盖该操作的分布式锁，且处于同一事务中。
     */
    public long changeInTransaction(StockChangeCmd cmd) {
        validate(cmd);
        return doChange(cmd);
    }

    private long doChange(StockChangeCmd cmd) {
        Long productId = cmd.getProductId();
        Long warehouseId = cmd.getWarehouseId();
        long delta = cmd.getQuantity();

        ensureStockRow(productId, warehouseId);
        BizStock before = findStock(productId, warehouseId);
        long beforeAvailable = before == null ? 0 : before.getAvailableQuantity();

        // 原子条件更新：affected = 0 即库存不足，抛异常回滚本次事务
        int affected = baseMapper.changeQuantity(productId, warehouseId, delta);
        if (affected == 0) {
            log.warn("[stock] 扣减失败(库存不足) productId={} warehouseId={} delta={} available={}",
                    productId, warehouseId, delta, beforeAvailable);
            throw new BizException(ResultCode.STOCK_NOT_ENOUGH,
                    "库存不足，商品[" + productId + "] 可用量 " + beforeAvailable + "，需扣减 " + (-delta));
        }

        BizStock after = findStock(productId, warehouseId);
        long afterAvailable = after == null ? beforeAvailable + delta : after.getAvailableQuantity();
        writeFlow(cmd, beforeAvailable, afterAvailable);
        return afterAvailable;
    }

    private void validate(StockChangeCmd cmd) {
        if (cmd == null || cmd.getProductId() == null || cmd.getWarehouseId() == null) {
            throw new BizException(ResultCode.VALIDATION_ERROR, "商品与仓库不能为空");
        }
        if (cmd.getQuantity() == null || cmd.getQuantity() == 0) {
            throw new BizException(ResultCode.VALIDATION_ERROR, "变动数量不能为 0");
        }
    }

    private String lockKey(Long productId, Long warehouseId) {
        return CacheKey.LOCK_STOCK + productId + ":" + warehouseId;
    }

    private void ensureStockRow(Long productId, Long warehouseId) {
        if (findStock(productId, warehouseId) != null) return;
        BizStock stock = new BizStock();
        stock.setId(IdWorker.getId());
        stock.setProductId(productId);
        stock.setWarehouseId(warehouseId);
        stock.setCreateBy(SecurityUtils.userId());
        stock.setCreateDept(SecurityUtils.deptId());
        // INSERT IGNORE：并发初始化时由唯一键兜底，不抛异常
        baseMapper.insertIgnore(stock);
    }

    private BizStock findStock(Long productId, Long warehouseId) {
        return getOne(Wrappers.lambdaQuery(BizStock.class)
                .eq(BizStock::getProductId, productId)
                .eq(BizStock::getWarehouseId, warehouseId)
                .last("LIMIT 1"), false);
    }

    private void writeFlow(StockChangeCmd cmd, long before, long after) {
        BizStockFlow flow = new BizStockFlow();
        flow.setFlowNo("SF" + IdWorker.getIdStr());
        flow.setProductId(cmd.getProductId());
        flow.setWarehouseId(cmd.getWarehouseId());
        flow.setBizType(cmd.getBizType());
        flow.setBizNo(cmd.getBizNo());
        flow.setQuantityBefore(before);
        flow.setQuantityChange(cmd.getQuantity());
        flow.setQuantityAfter(after);
        flow.setOperatorId(SecurityUtils.userId());
        flow.setOperatorName(SecurityUtils.username());
        flow.setOperTime(LocalDateTime.now());
        flow.setRemark(cmd.getRemark());
        flowMapper.insert(flow);
    }

    // ------------------------------ 查询 ------------------------------

    public PageResult<StockVO> page(StockQuery q) {
        IPage<StockVO> page = baseMapper.selectStockPage(new Page<>(q.getPageNum(), q.getPageSize()), q);
        page.getRecords().forEach(StockVO::applyDerived);
        return PageResult.of(page.getTotal(), page.getRecords(), page.getCurrent(), page.getSize());
    }

    public java.util.List<StockVO> listAll(StockQuery q) {
        q.setPageSize(500);
        return page(q).getRecords();
    }

    public long totalQuantity() { return baseMapper.totalQuantity(); }

    public int lowStockCount() { return baseMapper.countLowStock(); }
}

package com.enterprise.module.inventory.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enterprise.common.core.BizException;
import com.enterprise.common.core.CacheKey;
import com.enterprise.common.core.PageResult;
import com.enterprise.common.core.ResultCode;
import com.enterprise.common.redis.service.DistributedLockTemplate;
import com.enterprise.common.security.util.SecurityUtils;
import com.enterprise.module.inventory.dto.CheckCmd;
import com.enterprise.module.inventory.dto.StockChangeCmd;
import com.enterprise.module.inventory.entity.*;
import com.enterprise.module.inventory.mapper.BizInventoryCheckItemMapper;
import com.enterprise.module.inventory.mapper.BizInventoryCheckMapper;
import com.enterprise.module.inventory.mapper.BizStockMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** 盘点：审核时按差异生成盘盈/盘亏流水，全过程在同一事务与单据锁内。 */
@Service
@RequiredArgsConstructor
public class InventoryCheckService extends ServiceImpl<BizInventoryCheckMapper, BizInventoryCheck> {

    private final BizInventoryCheckItemMapper itemMapper;
    private final BizStockMapper stockMapper;
    private final StockService stockService;
    private final DistributedLockTemplate lockTemplate;
    private final TransactionTemplate transactionTemplate;

    public PageResult<BizInventoryCheck> page(long pageNum, long pageSize, Integer status) {
        var w = Wrappers.lambdaQuery(BizInventoryCheck.class)
                .eq(status != null, BizInventoryCheck::getStatus, status)
                .orderByDesc(BizInventoryCheck::getCreateTime);
        var p = page(new Page<>(pageNum, pageSize), w);
        return PageResult.of(p.getTotal(), p.getRecords(), p.getCurrent(), p.getSize());
    }

    public List<BizInventoryCheckItem> items(Long checkId) {
        return itemMapper.selectList(Wrappers.lambdaQuery(BizInventoryCheckItem.class)
                .eq(BizInventoryCheckItem::getCheckId, checkId));
    }

    public Long create(CheckCmd cmd) {
        BizInventoryCheck check = new BizInventoryCheck();
        check.setCheckNo("PD" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        check.setWarehouseId(cmd.getWarehouseId());
        check.setCheckDate(cmd.getCheckDate() == null ? LocalDate.now() : cmd.getCheckDate());
        check.setStatus(1);   // 待审核
        check.setRemark(cmd.getRemark());
        save(check);
        for (CheckCmd.Item item : cmd.getItems()) {
            BizStock stock = stockMapper.selectOne(Wrappers.lambdaQuery(BizStock.class)
                    .eq(BizStock::getProductId, item.getProductId())
                    .eq(BizStock::getWarehouseId, cmd.getWarehouseId()).last("LIMIT 1"));
            long systemQty = stock == null ? 0 : stock.getQuantity();
            BizInventoryCheckItem entity = new BizInventoryCheckItem();
            entity.setCheckId(check.getId());
            entity.setProductId(item.getProductId());
            entity.setSystemQuantity(systemQty);
            entity.setActualQuantity(item.getActualQuantity());
            entity.setDiffQuantity(item.getActualQuantity() - systemQty);
            entity.setRemark(item.getRemark());
            itemMapper.insert(entity);
        }
        return check.getId();
    }

    /** 审核：差异入库/出库 + 状态流转，锁 + 编程式事务保证原子 */
    public void audit(Long checkId) {
        lockTemplate.execute(CacheKey.LOCK_ORDER + "check:" + checkId, 5, -1, () -> {
            transactionTemplate.executeWithoutResult(status -> {
                BizInventoryCheck check = getById(checkId);
                if (check == null) throw new BizException(ResultCode.DATA_NOT_FOUND, "盘点单不存在");
                if (check.getStatus() != 1) throw new BizException(ResultCode.ORDER_STATUS_ILLEGAL, "该盘点单已处理");

                for (BizInventoryCheckItem item : items(checkId)) {
                    long diff = item.getDiffQuantity();
                    if (diff == 0) continue;
                    StockChangeCmd change = StockChangeCmd.of(item.getProductId(), check.getWarehouseId(), diff,
                            diff > 0 ? "CHECK_GAIN" : "CHECK_LOSS", check.getCheckNo(), "库存盘点差异调整");
                    stockService.changeInTransaction(change);
                }
                BizInventoryCheck update = new BizInventoryCheck();
                update.setId(checkId);
                update.setStatus(2);
                update.setAuditBy(SecurityUtils.userId());
                update.setAuditTime(LocalDateTime.now());
                updateById(update);
            });
            return null;
        });
    }
}

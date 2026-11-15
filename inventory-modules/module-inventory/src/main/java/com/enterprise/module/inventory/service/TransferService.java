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
import com.enterprise.module.inventory.dto.StockChangeCmd;
import com.enterprise.module.inventory.dto.TransferCmd;
import com.enterprise.module.inventory.entity.BizTransfer;
import com.enterprise.module.inventory.mapper.BizTransferMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/** 调拨：调出仓扣减 + 调入仓增加，两条流水在同一事务内要么全成要么全败。 */
@Service
@RequiredArgsConstructor
public class TransferService extends ServiceImpl<BizTransferMapper, BizTransfer> {

    private final StockService stockService;
    private final DistributedLockTemplate lockTemplate;
    private final TransactionTemplate transactionTemplate;

    public PageResult<BizTransfer> page(long pageNum, long pageSize, Integer status) {
        var p = page(new Page<>(pageNum, pageSize),
                Wrappers.lambdaQuery(BizTransfer.class)
                        .eq(status != null, BizTransfer::getStatus, status)
                        .orderByDesc(BizTransfer::getCreateTime));
        return PageResult.of(p.getTotal(), p.getRecords(), p.getCurrent(), p.getSize());
    }

    public Long create(TransferCmd cmd) {
        if (cmd.getQuantity() == null || cmd.getQuantity() <= 0) {
            throw new BizException(ResultCode.VALIDATION_ERROR, "调拨数量必须大于 0");
        }
        if (java.util.Objects.equals(cmd.getFromWarehouseId(), cmd.getToWarehouseId())) {
            throw new BizException(ResultCode.VALIDATION_ERROR, "调出与调入仓库不能相同");
        }
        BizTransfer transfer = new BizTransfer();
        transfer.setTransferNo("DB" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        transfer.setFromWarehouseId(cmd.getFromWarehouseId());
        transfer.setToWarehouseId(cmd.getToWarehouseId());
        transfer.setProductId(cmd.getProductId());
        transfer.setQuantity(cmd.getQuantity());
        transfer.setStatus(1);
        transfer.setRemark(cmd.getRemark());
        save(transfer);
        return transfer.getId();
    }

    public void audit(Long transferId) {
        lockTemplate.execute(CacheKey.LOCK_ORDER + "transfer:" + transferId, 5, -1, () -> {
            transactionTemplate.executeWithoutResult(status -> {
                BizTransfer transfer = getById(transferId);
                if (transfer == null) throw new BizException(ResultCode.DATA_NOT_FOUND, "调拨单不存在");
                if (transfer.getStatus() != 1) throw new BizException(ResultCode.ORDER_STATUS_ILLEGAL, "该调拨单已处理");

                stockService.changeInTransaction(StockChangeCmd.of(
                        transfer.getProductId(), transfer.getFromWarehouseId(), -transfer.getQuantity(),
                        "TRANSFER_OUT", transfer.getTransferNo(), "调拨出库"));
                stockService.changeInTransaction(StockChangeCmd.of(
                        transfer.getProductId(), transfer.getToWarehouseId(), transfer.getQuantity(),
                        "TRANSFER_IN", transfer.getTransferNo(), "调拨入库"));

                BizTransfer update = new BizTransfer();
                update.setId(transferId);
                update.setStatus(2);
                update.setAuditBy(SecurityUtils.userId());
                update.setAuditTime(LocalDateTime.now());
                updateById(update);
            });
            return null;
        });
    }
}

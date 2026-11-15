package com.enterprise.module.purchase.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.enterprise.common.mybatis.domain.BaseEntity;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * biz_purchase_order（biz_purchase_order）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_purchase_order")
public class BizPurchaseOrder extends BaseEntity {

    /** 采购单号 */
    @TableField("order_no")
    @NotBlank(message = "采购单号不能为空")
    private String orderNo;

    /** 供应商 */
    @TableField("supplier_id")
    @NotNull(message = "供应商不能为空")
    private Long supplierId;

    /** 入库仓库 */
    @TableField("warehouse_id")
    @NotNull(message = "入库仓库不能为空")
    private Long warehouseId;

    /** 总金额 */
    @TableField("total_amount")
    private BigDecimal totalAmount;

    /** 0草稿 1待审核 2已审核 3已入库 4已取消 5驳回 */
    @TableField("status")
    private Integer status;

    /** 期望到货日 */
    @TableField("expect_date")
    private LocalDate expectDate;

    /** 审核人 */
    @TableField("audit_by")
    private Long auditBy;

    /** 审核时间 */
    @TableField("audit_time")
    private LocalDateTime auditTime;

    /** 超时提醒已发送 */
    @TableField("timeout_notified")
    private Integer timeoutNotified;

    /** 备注 */
    @TableField("remark")
    private String remark;
}

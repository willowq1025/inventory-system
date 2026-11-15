package com.enterprise.module.sale.entity;

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
 * biz_sale_order（biz_sale_order）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_sale_order")
public class BizSaleOrder extends BaseEntity {

    /** 销售单号 */
    @TableField("order_no")
    @NotBlank(message = "销售单号不能为空")
    private String orderNo;

    /** 客户 */
    @TableField("customer_id")
    @NotNull(message = "客户不能为空")
    private Long customerId;

    /** 出库仓库 */
    @TableField("warehouse_id")
    @NotNull(message = "出库仓库不能为空")
    private Long warehouseId;

    /** 总金额 */
    @TableField("total_amount")
    private BigDecimal totalAmount;

    /** 0草稿 1待审核 2已审核 3已出库 4已取消 5驳回 */
    @TableField("status")
    private Integer status;

    /** 期望发货日 */
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

package com.enterprise.module.inventory.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.enterprise.common.mybatis.domain.BaseEntity;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * biz_stock_flow（biz_stock_flow）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_stock_flow")
public class BizStockFlow extends BaseEntity {

    /** 流水号 */
    @TableField("flow_no")
    @NotBlank(message = "流水号不能为空")
    private String flowNo;

    /** 商品ID */
    @TableField("product_id")
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    /** 仓库ID */
    @TableField("warehouse_id")
    @NotNull(message = "仓库ID不能为空")
    private Long warehouseId;

    /** 业务类型 */
    @TableField("biz_type")
    @NotBlank(message = "业务类型不能为空")
    private String bizType;

    /** 业务单号 */
    @TableField("biz_no")
    private String bizNo;

    /** 变动前 */
    @TableField("quantity_before")
    private long quantityBefore;

    /** 变动量 */
    @TableField("quantity_change")
    private long quantityChange;

    /** 变动后 */
    @TableField("quantity_after")
    private long quantityAfter;

    /** 操作人 */
    @TableField("operator_id")
    private Long operatorId;

    /** 操作人名 */
    @TableField("operator_name")
    private String operatorName;

    /** 业务时间 */
    @TableField("oper_time")
    private LocalDateTime operTime;

    /** 备注 */
    @TableField("remark")
    private String remark;
}

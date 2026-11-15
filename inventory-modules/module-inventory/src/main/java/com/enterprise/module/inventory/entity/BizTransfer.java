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
 * biz_transfer（biz_transfer）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_transfer")
public class BizTransfer extends BaseEntity {

    /** 调拨单号 */
    @TableField("transfer_no")
    @NotBlank(message = "调拨单号不能为空")
    private String transferNo;

    /** 调出仓库 */
    @TableField("from_warehouse_id")
    @NotNull(message = "调出仓库不能为空")
    private Long fromWarehouseId;

    /** 调入仓库 */
    @TableField("to_warehouse_id")
    @NotNull(message = "调入仓库不能为空")
    private Long toWarehouseId;

    /** 商品ID */
    @TableField("product_id")
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    /** 数量 */
    @TableField("quantity")
    private long quantity;

    /** 0草稿 1待审核 2已完成 3已取消 */
    @TableField("status")
    private Integer status;

    /** 审核人 */
    @TableField("audit_by")
    private Long auditBy;

    /** 审核时间 */
    @TableField("audit_time")
    private LocalDateTime auditTime;

    /** 备注 */
    @TableField("remark")
    private String remark;
}

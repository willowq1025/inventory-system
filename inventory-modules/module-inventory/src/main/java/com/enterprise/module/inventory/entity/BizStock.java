package com.enterprise.module.inventory.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.enterprise.common.mybatis.domain.BaseEntity;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.NotNull;

/**
 * biz_stock（biz_stock）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_stock")
public class BizStock extends BaseEntity {

    /** 商品ID */
    @TableField("product_id")
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    /** 仓库ID */
    @TableField("warehouse_id")
    @NotNull(message = "仓库ID不能为空")
    private Long warehouseId;

    /** 在库总量 */
    @TableField("quantity")
    private long quantity;

    /** 锁定量 */
    @TableField("locked_quantity")
    private long lockedQuantity;

    /** 可用量 */
    @TableField("available_quantity")
    private long availableQuantity;

    /** 备注 */
    @TableField("remark")
    private String remark;
}

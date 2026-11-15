package com.enterprise.module.inventory.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * biz_inventory_check_item（biz_inventory_check_item）
 */
@Data
@TableName("biz_inventory_check_item")
public class BizInventoryCheckItem {

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 盘点单ID */
    @TableField("check_id")
    @NotNull(message = "盘点单ID不能为空")
    private Long checkId;

    /** 商品ID */
    @TableField("product_id")
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    /** 账面数量 */
    @TableField("system_quantity")
    private long systemQuantity;

    /** 实盘数量 */
    @TableField("actual_quantity")
    private long actualQuantity;

    /** 差异量 */
    @TableField("diff_quantity")
    private long diffQuantity;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 逻辑删除 */
    @TableLogic
    private Integer deleted;

    /** 备注 */
    @TableField("remark")
    private String remark;
}

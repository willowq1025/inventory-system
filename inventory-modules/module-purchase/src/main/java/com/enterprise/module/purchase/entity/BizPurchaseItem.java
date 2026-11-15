package com.enterprise.module.purchase.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * biz_purchase_item（biz_purchase_item）
 */
@Data
@TableName("biz_purchase_item")
public class BizPurchaseItem {

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 采购单ID */
    @TableField("order_id")
    @NotNull(message = "采购单ID不能为空")
    private Long orderId;

    /** 商品ID */
    @TableField("product_id")
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    /** 数量 */
    @TableField("quantity")
    private long quantity;

    /** 已入库数量 */
    @TableField("received_quantity")
    private long receivedQuantity;

    /** 单价 */
    @TableField("unit_price")
    private BigDecimal unitPrice;

    /** 小计 */
    @TableField("amount")
    private BigDecimal amount;

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

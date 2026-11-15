package com.enterprise.module.sale.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * biz_sale_item（biz_sale_item）
 */
@Data
@TableName("biz_sale_item")
public class BizSaleItem {

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 销售单ID */
    @TableField("order_id")
    @NotNull(message = "销售单ID不能为空")
    private Long orderId;

    /** 商品ID */
    @TableField("product_id")
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    /** 数量 */
    @TableField("quantity")
    private long quantity;

    /** 已出库数量 */
    @TableField("delivered_quantity")
    private long deliveredQuantity;

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

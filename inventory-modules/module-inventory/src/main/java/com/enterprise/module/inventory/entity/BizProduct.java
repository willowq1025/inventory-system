package com.enterprise.module.inventory.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.enterprise.common.mybatis.domain.BaseEntity;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

/**
 * biz_product（biz_product）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_product")
public class BizProduct extends BaseEntity {

    /** 分类ID */
    @TableField("category_id")
    private Long categoryId;

    /** SKU */
    @TableField("product_code")
    @NotBlank(message = "SKU不能为空")
    private String productCode;

    /** 商品名称 */
    @TableField("product_name")
    @NotBlank(message = "商品名称不能为空")
    private String productName;

    /** 规格 */
    @TableField("spec")
    private String spec;

    /** 单位 */
    @TableField("unit")
    private String unit;

    /** 品牌 */
    @TableField("brand")
    private String brand;

    /** 条码 */
    @TableField("barcode")
    private String barcode;

    /** 采购价 */
    @TableField("purchase_price")
    private BigDecimal purchasePrice;

    /** 销售价 */
    @TableField("sale_price")
    private BigDecimal salePrice;

    /** 安全库存 */
    @TableField("safety_stock")
    private long safetyStock;

    /** 状态 */
    @TableField("status")
    private Integer status;

    /** 图片 */
    @TableField("image_url")
    private String imageUrl;

    /** 备注 */
    @TableField("remark")
    private String remark;
}

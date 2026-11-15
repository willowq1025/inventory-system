package com.enterprise.module.inventory.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import cn.hutool.core.bean.BeanUtil;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import com.enterprise.module.inventory.entity.BizProduct;
/** 商品导入导出模型（与数据库实体解耦，避免导出内部字段）。 */
@Data
public class BizProductExcel {

    /** 分类ID */
    @ExcelProperty("分类ID")
    @ColumnWidth(20)
    private Long categoryId;

    /** SKU */
    @ExcelProperty("SKU")
    @ColumnWidth(20)
    private String productCode;

    /** 商品名称 */
    @ExcelProperty("商品名称")
    @ColumnWidth(20)
    private String productName;

    /** 规格 */
    @ExcelProperty("规格")
    @ColumnWidth(20)
    private String spec;

    /** 单位 */
    @ExcelProperty("单位")
    @ColumnWidth(20)
    private String unit;

    /** 品牌 */
    @ExcelProperty("品牌")
    @ColumnWidth(20)
    private String brand;

    /** 条码 */
    @ExcelProperty("条码")
    @ColumnWidth(20)
    private String barcode;

    /** 采购价 */
    @ExcelProperty("采购价")
    @ColumnWidth(20)
    private BigDecimal purchasePrice;

    /** 销售价 */
    @ExcelProperty("销售价")
    @ColumnWidth(20)
    private BigDecimal salePrice;

    /** 安全库存 */
    @ExcelProperty("安全库存")
    @ColumnWidth(20)
    private long safetyStock;

    /** 状态 */
    @ExcelProperty("状态")
    @ColumnWidth(20)
    private Integer status;

    /** 图片 */
    @ExcelProperty("图片")
    @ColumnWidth(20)
    private String imageUrl;

    /** 备注 */
    @ExcelProperty("备注")
    @ColumnWidth(20)
    private String remark;

    public static BizProductExcel from(BizProduct entity) {
        return BeanUtil.copyProperties(entity, BizProductExcel.class);
    }

    public BizProduct toEntity() {
        return BeanUtil.copyProperties(this, BizProduct.class);
    }
}

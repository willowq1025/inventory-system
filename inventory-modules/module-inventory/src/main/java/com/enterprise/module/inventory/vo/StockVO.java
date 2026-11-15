package com.enterprise.module.inventory.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.time.LocalDateTime;

/** 库存台账视图（同时作为导出模型）。 */
@Data
public class StockVO {

    private Long id;
    private Long productId;
    private Long warehouseId;

    @ExcelProperty("库存数量")
    @ColumnWidth(14)
    private long quantity;

    @ExcelProperty("锁定数量")
    @ColumnWidth(14)
    private long lockedQuantity;

    @ExcelProperty("可用数量")
    @ColumnWidth(14)
    private long availableQuantity;

    @ExcelProperty("商品编码")
    @ColumnWidth(20)
    private String productCode;

    @ExcelProperty("商品名称")
    @ColumnWidth(28)
    private String productName;

    @ExcelProperty("单位")
    @ColumnWidth(10)
    private String unit;

    @ExcelProperty("安全库存")
    @ColumnWidth(14)
    private long safetyStock;

    @ExcelProperty("仓库")
    @ColumnWidth(20)
    private String warehouseName;

    @ExcelProperty("是否低库存")
    @ColumnWidth(14)
    private String lowStockText;

    private LocalDateTime updateTime;

    public boolean isLowStock() { return availableQuantity <= safetyStock; }

    public void applyDerived() {
        setLowStockText(isLowStock() ? "是" : "否");
    }
}

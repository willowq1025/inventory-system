package com.enterprise.module.report.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.enterprise.module.report.vo.PssVO;
import lombok.Data;

@Data
public class PssExcel {

    @ExcelProperty("商品编码")
    @ColumnWidth(20)
    private String productCode;

    @ExcelProperty("商品名称")
    @ColumnWidth(28)
    private String productName;

    @ExcelProperty("单位")
    @ColumnWidth(10)
    private String unit;

    @ExcelProperty("仓库")
    @ColumnWidth(20)
    private String warehouseName;

    @ExcelProperty("期初数量")
    @ColumnWidth(14)
    private long openingQty;

    @ExcelProperty("入库数量")
    @ColumnWidth(14)
    private long inQty;

    @ExcelProperty("出库数量")
    @ColumnWidth(14)
    private long outQty;

    @ExcelProperty("结存数量")
    @ColumnWidth(14)
    private long closingQty;

    public static PssExcel from(PssVO vo) {
        PssExcel excel = new PssExcel();
        excel.setProductCode(vo.getProductCode());
        excel.setProductName(vo.getProductName());
        excel.setUnit(vo.getUnit());
        excel.setWarehouseName(vo.getWarehouseName());
        excel.setOpeningQty(vo.getOpeningQty());
        excel.setInQty(vo.getInQty());
        excel.setOutQty(vo.getOutQty());
        excel.setClosingQty(vo.getClosingQty());
        return excel;
    }
}

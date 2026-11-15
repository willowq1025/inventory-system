package com.enterprise.module.inventory.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import cn.hutool.core.bean.BeanUtil;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.enterprise.module.inventory.entity.BizStockFlow;
/** 库存流水导入导出模型（与数据库实体解耦，避免导出内部字段）。 */
@Data
public class BizStockFlowExcel {

    /** 流水号 */
    @ExcelProperty("流水号")
    @ColumnWidth(20)
    private String flowNo;

    /** 商品ID */
    @ExcelProperty("商品ID")
    @ColumnWidth(20)
    private Long productId;

    /** 仓库ID */
    @ExcelProperty("仓库ID")
    @ColumnWidth(20)
    private Long warehouseId;

    /** 业务类型 */
    @ExcelProperty("业务类型")
    @ColumnWidth(20)
    private String bizType;

    /** 业务单号 */
    @ExcelProperty("业务单号")
    @ColumnWidth(20)
    private String bizNo;

    /** 变动前 */
    @ExcelProperty("变动前")
    @ColumnWidth(20)
    private long quantityBefore;

    /** 变动量 */
    @ExcelProperty("变动量")
    @ColumnWidth(20)
    private long quantityChange;

    /** 变动后 */
    @ExcelProperty("变动后")
    @ColumnWidth(20)
    private long quantityAfter;

    /** 操作人 */
    @ExcelProperty("操作人")
    @ColumnWidth(20)
    private Long operatorId;

    /** 操作人名 */
    @ExcelProperty("操作人名")
    @ColumnWidth(20)
    private String operatorName;

    /** 业务时间 */
    @ExcelProperty("业务时间")
    @ColumnWidth(20)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operTime;

    /** 备注 */
    @ExcelProperty("备注")
    @ColumnWidth(20)
    private String remark;

    public static BizStockFlowExcel from(BizStockFlow entity) {
        return BeanUtil.copyProperties(entity, BizStockFlowExcel.class);
    }

    public BizStockFlow toEntity() {
        return BeanUtil.copyProperties(this, BizStockFlow.class);
    }
}

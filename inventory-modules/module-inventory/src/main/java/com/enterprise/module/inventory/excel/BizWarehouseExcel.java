package com.enterprise.module.inventory.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import cn.hutool.core.bean.BeanUtil;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import com.enterprise.module.inventory.entity.BizWarehouse;
/** 仓库导入导出模型（与数据库实体解耦，避免导出内部字段）。 */
@Data
public class BizWarehouseExcel {

    /** 仓库编码 */
    @ExcelProperty("仓库编码")
    @ColumnWidth(20)
    private String warehouseCode;

    /** 仓库名称 */
    @ExcelProperty("仓库名称")
    @ColumnWidth(20)
    private String warehouseName;

    /** 地址 */
    @ExcelProperty("地址")
    @ColumnWidth(20)
    private String address;

    /** 负责人 */
    @ExcelProperty("负责人")
    @ColumnWidth(20)
    private Long managerId;

    /** 电话 */
    @ExcelProperty("电话")
    @ColumnWidth(20)
    private String phone;

    /** 状态 */
    @ExcelProperty("状态")
    @ColumnWidth(20)
    private Integer status;

    /** 备注 */
    @ExcelProperty("备注")
    @ColumnWidth(20)
    private String remark;

    public static BizWarehouseExcel from(BizWarehouse entity) {
        return BeanUtil.copyProperties(entity, BizWarehouseExcel.class);
    }

    public BizWarehouse toEntity() {
        return BeanUtil.copyProperties(this, BizWarehouse.class);
    }
}

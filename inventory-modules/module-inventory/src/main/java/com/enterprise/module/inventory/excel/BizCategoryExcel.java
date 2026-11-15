package com.enterprise.module.inventory.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import cn.hutool.core.bean.BeanUtil;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import com.enterprise.module.inventory.entity.BizCategory;
/** 商品分类导入导出模型（与数据库实体解耦，避免导出内部字段）。 */
@Data
public class BizCategoryExcel {

    /** 父分类 */
    @ExcelProperty("父分类")
    @ColumnWidth(20)
    private Long parentId;

    /** 分类名称 */
    @ExcelProperty("分类名称")
    @ColumnWidth(20)
    private String categoryName;

    /** 分类编码 */
    @ExcelProperty("分类编码")
    @ColumnWidth(20)
    private String categoryCode;

    /** 排序 */
    @ExcelProperty("排序")
    @ColumnWidth(20)
    private Integer sort;

    /** 状态 */
    @ExcelProperty("状态")
    @ColumnWidth(20)
    private Integer status;

    /** 备注 */
    @ExcelProperty("备注")
    @ColumnWidth(20)
    private String remark;

    public static BizCategoryExcel from(BizCategory entity) {
        return BeanUtil.copyProperties(entity, BizCategoryExcel.class);
    }

    public BizCategory toEntity() {
        return BeanUtil.copyProperties(this, BizCategory.class);
    }
}

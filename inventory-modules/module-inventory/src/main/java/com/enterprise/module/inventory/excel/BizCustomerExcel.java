package com.enterprise.module.inventory.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import cn.hutool.core.bean.BeanUtil;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import com.enterprise.module.inventory.entity.BizCustomer;
/** 客户导入导出模型（与数据库实体解耦，避免导出内部字段）。 */
@Data
public class BizCustomerExcel {

    /** 客户编码 */
    @ExcelProperty("客户编码")
    @ColumnWidth(20)
    private String customerCode;

    /** 客户名称 */
    @ExcelProperty("客户名称")
    @ColumnWidth(20)
    private String customerName;

    /** 联系人 */
    @ExcelProperty("联系人")
    @ColumnWidth(20)
    private String contact;

    /** 电话 */
    @ExcelProperty("电话")
    @ColumnWidth(20)
    private String phone;

    /** 邮箱 */
    @ExcelProperty("邮箱")
    @ColumnWidth(20)
    private String email;

    /** 地址 */
    @ExcelProperty("地址")
    @ColumnWidth(20)
    private String address;

    /** 状态 */
    @ExcelProperty("状态")
    @ColumnWidth(20)
    private Integer status;

    /** 备注 */
    @ExcelProperty("备注")
    @ColumnWidth(20)
    private String remark;

    public static BizCustomerExcel from(BizCustomer entity) {
        return BeanUtil.copyProperties(entity, BizCustomerExcel.class);
    }

    public BizCustomer toEntity() {
        return BeanUtil.copyProperties(this, BizCustomer.class);
    }
}

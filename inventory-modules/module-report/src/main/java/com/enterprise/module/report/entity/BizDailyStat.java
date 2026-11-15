package com.enterprise.module.report.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * biz_daily_stat（biz_daily_stat）
 */
@Data
@TableName("biz_daily_stat")
public class BizDailyStat {

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 统计日期 */
    @TableField("stat_date")
    @NotNull(message = "统计日期不能为空")
    private LocalDate statDate;

    /** 采购额 */
    @TableField("purchase_amount")
    private BigDecimal purchaseAmount;

    /** 销售额 */
    @TableField("sale_amount")
    private BigDecimal saleAmount;

    /** 采购单数 */
    @TableField("purchase_count")
    private Integer purchaseCount;

    /** 销售单数 */
    @TableField("sale_count")
    private Integer saleCount;

    /** 库存总量 */
    @TableField("stock_total")
    private long stockTotal;

    /** 低库存品项 */
    @TableField("low_stock_count")
    private Integer lowStockCount;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

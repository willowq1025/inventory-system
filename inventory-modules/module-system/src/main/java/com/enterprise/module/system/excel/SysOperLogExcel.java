package com.enterprise.module.system.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import cn.hutool.core.bean.BeanUtil;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.enterprise.module.system.entity.SysOperLog;
/** 审计日志导入导出模型（与数据库实体解耦，避免导出内部字段）。 */
@Data
public class SysOperLogExcel {

    /** 主键 */
    @ExcelProperty("主键")
    @ColumnWidth(20)
    private Long id;

    /** 链路ID */
    @ExcelProperty("链路ID")
    @ColumnWidth(20)
    private String traceId;

    /** 业务标题 */
    @ExcelProperty("业务标题")
    @ColumnWidth(20)
    private String title;

    /** 模块 */
    @ExcelProperty("模块")
    @ColumnWidth(20)
    private String module;

    /** 业务类型 */
    @ExcelProperty("业务类型")
    @ColumnWidth(20)
    private String businessType;

    /** 执行方法 */
    @ExcelProperty("执行方法")
    @ColumnWidth(20)
    private String method;

    /** HTTP方法 */
    @ExcelProperty("HTTP方法")
    @ColumnWidth(20)
    private String requestMethod;

    /** 请求地址 */
    @ExcelProperty("请求地址")
    @ColumnWidth(20)
    private String requestUrl;

    /** 操作人ID */
    @ExcelProperty("操作人ID")
    @ColumnWidth(20)
    private Long operId;

    /** 操作人 */
    @ExcelProperty("操作人")
    @ColumnWidth(20)
    private String operName;

    /** 操作人部门 */
    @ExcelProperty("操作人部门")
    @ColumnWidth(20)
    private Long operDept;

    /** IP */
    @ExcelProperty("IP")
    @ColumnWidth(20)
    private String operIp;

    /** 状态 */
    @ExcelProperty("状态")
    @ColumnWidth(20)
    private Integer status;

    /** 耗时 */
    @ExcelProperty("耗时")
    @ColumnWidth(20)
    private long costMs;

    /** 操作时间 */
    @ExcelProperty("操作时间")
    @ColumnWidth(20)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operTime;

    public static SysOperLogExcel from(SysOperLog entity) {
        return BeanUtil.copyProperties(entity, SysOperLogExcel.class);
    }

    public SysOperLog toEntity() {
        return BeanUtil.copyProperties(this, SysOperLog.class);
    }
}

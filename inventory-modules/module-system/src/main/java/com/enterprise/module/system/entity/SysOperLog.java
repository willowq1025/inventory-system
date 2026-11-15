package com.enterprise.module.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * sys_oper_log（sys_oper_log）
 */
@Data
@TableName("sys_oper_log")
public class SysOperLog {

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 链路ID */
    @TableField("trace_id")
    private String traceId;

    /** 业务标题 */
    @TableField("title")
    private String title;

    /** 模块 */
    @TableField("module")
    private String module;

    /** 业务类型 */
    @TableField("business_type")
    private String businessType;

    /** 执行方法 */
    @TableField("method")
    private String method;

    /** HTTP方法 */
    @TableField("request_method")
    private String requestMethod;

    /** 请求地址 */
    @TableField("request_url")
    private String requestUrl;

    /** 请求参数 */
    @TableField("request_param")
    private String requestParam;

    /** 响应结果 */
    @TableField("response_result")
    private String responseResult;

    /** 操作人ID */
    @TableField("oper_id")
    private Long operId;

    /** 操作人 */
    @TableField("oper_name")
    private String operName;

    /** 操作人部门 */
    @TableField("oper_dept")
    private Long operDept;

    /** IP */
    @TableField("oper_ip")
    private String operIp;

    /** UA */
    @TableField("user_agent")
    private String userAgent;

    /** 状态 */
    @TableField("status")
    private Integer status;

    /** 错误信息 */
    @TableField("error_msg")
    private String errorMsg;

    /** 耗时 */
    @TableField("cost_ms")
    private long costMs;

    /** 操作时间 */
    @TableField("oper_time")
    private LocalDateTime operTime;
}

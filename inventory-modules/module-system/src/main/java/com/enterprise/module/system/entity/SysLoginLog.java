package com.enterprise.module.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * sys_login_log（sys_login_log）
 */
@Data
@TableName("sys_login_log")
public class SysLoginLog {

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 链路ID */
    @TableField("trace_id")
    private String traceId;

    /** 账号 */
    @TableField("username")
    private String username;

    /** IP */
    @TableField("ip")
    private String ip;

    /** 浏览器 */
    @TableField("browser")
    private String browser;

    /** 操作系统 */
    @TableField("os")
    private String os;

    /** 状态 */
    @TableField("status")
    private Integer status;

    /** 消息 */
    @TableField("message")
    private String message;

    /** 登录时间 */
    @TableField("login_time")
    private LocalDateTime loginTime;
}

package com.enterprise.module.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * sys_notice_read（sys_notice_read）
 */
@Data
@TableName("sys_notice_read")
public class SysNoticeRead {

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 通知ID */
    @TableField("notice_id")
    @NotNull(message = "通知ID不能为空")
    private Long noticeId;

    /** 用户ID */
    @TableField("user_id")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /** 是否已读 */
    @TableField("read_flag")
    private Integer readFlag;

    /** 阅读时间 */
    @TableField("read_time")
    private LocalDateTime readTime;
}

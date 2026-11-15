package com.enterprise.module.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.enterprise.common.mybatis.domain.BaseEntity;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.NotBlank;

/**
 * sys_notice（sys_notice）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_notice")
public class SysNotice extends BaseEntity {

    /** 标题 */
    @TableField("title")
    @NotBlank(message = "标题不能为空")
    private String title;

    /** 内容 */
    @TableField("content")
    private String content;

    /** 1通知 2公告 3预警 */
    @TableField("notice_type")
    private Integer noticeType;

    /** 1普通 2重要 3紧急 */
    @TableField("level")
    private Integer level;

    /** 1全体 2指定用户 3指定角色 */
    @TableField("target_type")
    private Integer targetType;

    /** 目标ID列表 */
    @TableField("target_ids")
    private String targetIds;

    /** 发送人 */
    @TableField("sender_id")
    private Long senderId;

    /** 备注 */
    @TableField("remark")
    private String remark;
}

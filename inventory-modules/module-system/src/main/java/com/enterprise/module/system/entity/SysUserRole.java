package com.enterprise.module.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotNull;

/**
 * sys_user_role（sys_user_role）
 */
@Data
@TableName("sys_user_role")
public class SysUserRole {

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 用户ID */
    @TableField("user_id")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /** 角色ID */
    @TableField("role_id")
    @NotNull(message = "角色ID不能为空")
    private Long roleId;
}

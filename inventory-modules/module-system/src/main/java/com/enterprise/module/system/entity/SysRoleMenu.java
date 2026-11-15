package com.enterprise.module.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotNull;

/**
 * sys_role_menu（sys_role_menu）
 */
@Data
@TableName("sys_role_menu")
public class SysRoleMenu {

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 角色ID */
    @TableField("role_id")
    @NotNull(message = "角色ID不能为空")
    private Long roleId;

    /** 菜单ID */
    @TableField("menu_id")
    @NotNull(message = "菜单ID不能为空")
    private Long menuId;
}

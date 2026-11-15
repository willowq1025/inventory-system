package com.enterprise.module.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotNull;

/**
 * sys_role_dept（sys_role_dept）
 */
@Data
@TableName("sys_role_dept")
public class SysRoleDept {

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 角色ID */
    @TableField("role_id")
    @NotNull(message = "角色ID不能为空")
    private Long roleId;

    /** 部门ID */
    @TableField("dept_id")
    @NotNull(message = "部门ID不能为空")
    private Long deptId;
}

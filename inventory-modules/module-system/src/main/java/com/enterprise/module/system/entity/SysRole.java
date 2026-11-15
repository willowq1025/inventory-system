package com.enterprise.module.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.enterprise.common.mybatis.domain.BaseEntity;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.NotBlank;

/**
 * sys_role（sys_role）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class SysRole extends BaseEntity {

    /** 角色名称 */
    @TableField("role_name")
    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    /** 角色标识 */
    @TableField("role_key")
    @NotBlank(message = "角色标识不能为空")
    private String roleKey;

    /** 显示顺序 */
    @TableField("sort")
    private Integer sort;

    /** 数据范围 1全部 2本部门及以下 3本部门 4本人 5自定义 */
    @TableField("data_scope")
    private Integer dataScope;

    /** 状态 */
    @TableField("status")
    private Integer status;

    /** 备注 */
    @TableField("remark")
    private String remark;
}

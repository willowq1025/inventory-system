package com.enterprise.module.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.enterprise.common.mybatis.domain.BaseEntity;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.NotBlank;

/**
 * sys_dept（sys_dept）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dept")
public class SysDept extends BaseEntity {

    /** 父部门ID */
    @TableField("parent_id")
    private Long parentId;

    /** 祖级列表 */
    @TableField("ancestors")
    private String ancestors;

    /** 部门名称 */
    @TableField("dept_name")
    @NotBlank(message = "部门名称不能为空")
    private String deptName;

    /** 部门编码 */
    @TableField("dept_code")
    @NotBlank(message = "部门编码不能为空")
    private String deptCode;

    /** 负责人 */
    @TableField("leader")
    private String leader;

    /** 电话 */
    @TableField("phone")
    private String phone;

    /** 邮箱 */
    @TableField("email")
    private String email;

    /** 显示顺序 */
    @TableField("sort")
    private Integer sort;

    /** 状态 */
    @TableField("status")
    private Integer status;

    /** 备注 */
    @TableField("remark")
    private String remark;
}

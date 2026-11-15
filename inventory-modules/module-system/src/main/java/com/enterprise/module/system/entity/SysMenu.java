package com.enterprise.module.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.enterprise.common.mybatis.domain.BaseEntity;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.NotBlank;

/**
 * sys_menu（sys_menu）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class SysMenu extends BaseEntity {

    /** 父菜单ID */
    @TableField("parent_id")
    private Long parentId;

    /** 菜单名称 */
    @TableField("menu_name")
    @NotBlank(message = "菜单名称不能为空")
    private String menuName;

    /** M目录 C菜单 F按钮接口权限 */
    @TableField("menu_type")
    @NotBlank(message = "M目录 C菜单 F按钮接口权限不能为空")
    private String menuType;

    /** 路由地址 */
    @TableField("path")
    private String path;

    /** 前端组件 */
    @TableField("component")
    private String component;

    /** 路由名称 */
    @TableField("route_name")
    private String routeName;

    /** 图标 */
    @TableField("icon")
    private String icon;

    /** 权限标识 */
    @TableField("permission")
    private String permission;

    /** 接口路径 */
    @TableField("api_path")
    private String apiPath;

    /** 接口方法 */
    @TableField("api_method")
    private String apiMethod;

    /** 显示顺序 */
    @TableField("sort")
    private Integer sort;

    /** 是否可见 */
    @TableField("visible")
    private Integer visible;

    /** 状态 */
    @TableField("status")
    private Integer status;

    /** 是否外链 */
    @TableField("is_frame")
    private Integer isFrame;

    /** 备注 */
    @TableField("remark")
    private String remark;
}

package com.enterprise.module.system.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class MenuCmd {

    private Long id;

    @NotNull(message = "父菜单不能为空")
    private Long parentId = 0L;

    @NotBlank(message = "菜单名称不能为空")
    private String menuName;

    @NotBlank(message = "菜单类型不能为空")
    @Pattern(regexp = "[MCF]", message = "菜单类型须为 M/C/F")
    private String menuType = "C";

    private String path;
    private String component;
    private String routeName;
    private String icon;
    /** 权限标识，F 类型必填 */
    private String permission;
    /** 接口权限：Ant 路径与 HTTP 方法 */
    private String apiPath;
    private String apiMethod;
    private Integer sort = 0;
    private Integer visible = 1;
    private Integer status = 1;
    private String remark;
}

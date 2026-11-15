package com.enterprise.module.system.dto;

import com.enterprise.common.core.validation.Create;
import com.enterprise.common.core.validation.Update;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class RoleCmd {

    @NotNull(message = "角色ID不能为空", groups = Update.class)
    private Long id;

    @NotBlank(message = "角色名称不能为空")
    @Size(max = 64)
    private String roleName;

    @NotBlank(message = "角色标识不能为空")
    @Pattern(regexp = "^[a-z][a-z0-9_]{1,63}$", message = "角色标识须为小写字母开头的字母数字下划线")
    private String roleKey;

    @NotNull(message = "数据范围不能为空")
    @Min(value = 1, message = "数据范围取值 1-5")
    @Max(value = 5, message = "数据范围取值 1-5")
    private Integer dataScope = 4;

    private Integer sort = 0;
    private Integer status = 1;
    private String remark;

    /** 菜单 / 接口权限 ID 集合 */
    private List<Long> menuIds;
    /** dataScope=5 时生效的自定义部门 */
    private List<Long> deptIds;
}

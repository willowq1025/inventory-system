package com.enterprise.module.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeptCmd {

    private Long id;

    @NotNull(message = "上级部门不能为空")
    private Long parentId = 0L;

    @NotBlank(message = "部门名称不能为空")
    private String deptName;

    @NotBlank(message = "部门编码不能为空")
    private String deptCode;

    private String leader;
    private String phone;
    private String email;
    private Integer sort = 0;
    private Integer status = 1;
    private String remark;
}

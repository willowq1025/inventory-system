package com.enterprise.module.system.dto;

import com.enterprise.common.core.validation.Create;
import com.enterprise.common.core.validation.Update;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

/** 用户新增 / 修改命令，使用校验分组区分场景。 */
@Data
public class UserCmd {

    @NotNull(message = "用户ID不能为空", groups = Update.class)
    private Long id;

    @NotBlank(message = "账号不能为空", groups = Create.class)
    @Size(max = 64, message = "账号长度不能超过 64")
    private String username;

    @NotBlank(message = "昵称不能为空")
    @Size(max = 64, message = "昵称长度不能超过 64")
    private String nickname;

    @NotBlank(message = "初始密码不能为空", groups = Create.class)
    @Size(min = 6, max = 64, message = "密码长度须在 6-64 之间")
    private String password;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    private Long deptId;
    private Integer status = 1;
    private Integer sex = 0;
    private String remark;
    private List<Long> roleIds;
}

package com.enterprise.module.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginCmd {

    @NotBlank(message = "账号不能为空")
    @Size(max = 64, message = "账号长度不能超过 64")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 64, message = "密码长度须在 6-64 之间")
    private String password;
}

package com.enterprise.module.system.controller;

import com.enterprise.common.core.Result;
import com.enterprise.common.log.annotation.AuditLog;
import com.enterprise.common.mybatis.datascope.IgnoreDataScope;
import com.enterprise.module.system.dto.LoginCmd;
import com.enterprise.module.system.service.AuthService;
import com.enterprise.module.system.vo.LoginVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** 认证入口（/api/auth/login 在 SecurityProperties.ignoreUrls 中放行）。 */
@Validated
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @IgnoreDataScope
    @AuditLog(title = "用户登录", module = "认证", businessType = "LOGIN", saveRequest = false)
    public Result<LoginVO> login(@Valid @RequestBody LoginCmd cmd) {
        return Result.ok(authService.login(cmd));
    }

    @GetMapping("/info")
    @IgnoreDataScope
    public Result<LoginVO> info() {
        return Result.ok(authService.info());
    }

    @PostMapping("/logout")
    @AuditLog(title = "用户退出", module = "认证", businessType = "LOGOUT", saveRequest = false)
    public Result<Void> logout() {
        authService.logout();
        return Result.ok();
    }

    /** 预留验证码接口（默认关闭，接入图形验证码后替换实现） */
    @GetMapping("/captcha")
    public Result<Map<String, Object>> captcha() {
        return Result.ok(Map.of("enabled", false));
    }
}

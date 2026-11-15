package com.enterprise.common.security.filter;

import com.enterprise.common.core.Result;
import com.enterprise.common.core.ResultCode;
import com.enterprise.common.core.util.JsonUtils;
import com.enterprise.common.security.model.ApiRule;
import com.enterprise.common.security.model.LoginUser;
import com.enterprise.common.security.spi.UserAuthProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 接口级权限二次校验：
 * sys_menu 中登记了 api_path / api_method 的 F 类型记录即为受控接口，
 * 请求命中规则时，登录人必须拥有对应 permission 标识（超管除外）。
 * 与 {@code @RequirePermission} 注解形成「集中登记 + 就近声明」双通道。
 */
@Slf4j
@RequiredArgsConstructor
public class ApiPermissionFilter extends OncePerRequestFilter {

    private final UserAuthProvider userAuthProvider;
    private final boolean enabled;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !enabled || "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser user)) {
            chain.doFilter(request, response);
            return;
        }
        String path = request.getServletPath();
        String method = request.getMethod();
        List<ApiRule> rules = userAuthProvider.listApiRules();
        for (ApiRule rule : rules) {
            if (rule.permission() == null || rule.path() == null) continue;
            boolean methodMatch = "*".equals(rule.method()) || method.equalsIgnoreCase(rule.method());
            if (methodMatch && pathMatcher.match(rule.path(), path)) {
                if (!user.hasPermission(rule.permission())) {
                    log.warn("[auth] 接口权限拒绝 userId={} {} {} require={}", user.getUserId(), method, path, rule.permission());
                    writeError(response, ResultCode.FORBIDDEN, "缺少接口权限: " + rule.permission());
                    return;
                }
                break;
            }
        }
        chain.doFilter(request, response);
    }

    private void writeError(HttpServletResponse response, ResultCode code, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(JsonUtils.toJson(Result.fail(code, message)));
    }
}

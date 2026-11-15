package com.enterprise.common.security.filter;

import com.enterprise.common.core.CommonConstant;
import com.enterprise.common.core.Result;
import com.enterprise.common.core.ResultCode;
import com.enterprise.common.core.context.UserContext;
import com.enterprise.common.core.util.JsonUtils;
import com.enterprise.common.security.jwt.JwtTokenProvider;
import com.enterprise.common.security.model.LoginUser;
import com.enterprise.common.security.spi.UserAuthProvider;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * JWT 认证过滤器：解析 token → 加载（缓存中的）登录主体 → 写入 SecurityContext + UserContext。
 * UserContext 在请求结束时必须清理，避免线程池复用导致身份串号。
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final UserAuthProvider userAuthProvider;
    private final JwtTokenProvider jwtTokenProvider;
    private final List<String> ignoreUrls;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return ignoreUrls.stream().anyMatch(p -> pathMatcher.match(p, path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String token = resolveToken(request);
        if (!StringUtils.hasText(token)) {
            chain.doFilter(request, response);
            return;
        }
        try {
            Long userId = jwtTokenProvider.getUserId(token);
            LoginUser loginUser = userAuthProvider.loadLoginUser(userId);
            if (loginUser == null || !loginUser.isEnabled()) {
                writeError(response, ResultCode.UNAUTHORIZED);
                return;
            }
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserContext.set(loginUser);
            chain.doFilter(request, response);
        } catch (JwtException e) {
            log.debug("[auth] token 校验失败: {}", e.getMessage());
            SecurityContextHolder.clearContext();
            writeError(response, ResultCode.TOKEN_EXPIRED);
        } finally {
            UserContext.clear();
            SecurityContextHolder.clearContext();
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(CommonConstant.TOKEN_HEADER);
        if (StringUtils.hasText(header) && header.startsWith(CommonConstant.TOKEN_PREFIX)) {
            return header.substring(CommonConstant.TOKEN_PREFIX.length());
        }
        return null;
    }

    private void writeError(HttpServletResponse response, ResultCode code) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(JsonUtils.toJson(Result.fail(code)));
    }
}

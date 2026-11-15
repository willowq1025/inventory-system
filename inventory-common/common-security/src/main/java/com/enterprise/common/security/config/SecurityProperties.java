package com.enterprise.common.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "inventory.security")
public class SecurityProperties {

    private Jwt jwt = new Jwt();
    /** 无需认证的路径（Ant 风格） */
    private List<String> ignoreUrls = new ArrayList<>(List.of(
            "/api/auth/login", "/api/auth/captcha", "/api/auth/logout",
            "/doc.html", "/webjars/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-resources/**",
            "/actuator/health", "/actuator/info", "/favicon.ico", "/error"));
    /** 是否启用「已登记接口 → 权限标识」的二次校验 */
    private boolean apiPermissionCheck = true;
    /** 同一账号是否允许多处同时在线 */
    private boolean allowConcurrentLogin = true;

    public static class Jwt {
        /** HS256 密钥，至少 32 字节；生产环境请通过环境变量注入 */
        private String secret = "inventory-system-default-jwt-secret-please-change-in-production-2026";
        private Duration accessTokenValidity = Duration.ofHours(8);
        private String issuer = "inventory-system";

        public String getSecret() { return secret; }
        public void setSecret(String secret) { this.secret = secret; }
        public Duration getAccessTokenValidity() { return accessTokenValidity; }
        public void setAccessTokenValidity(Duration accessTokenValidity) { this.accessTokenValidity = accessTokenValidity; }
        public String getIssuer() { return issuer; }
        public void setIssuer(String issuer) { this.issuer = issuer; }
    }

    public Jwt getJwt() { return jwt; }
    public void setJwt(Jwt jwt) { this.jwt = jwt; }
    public List<String> getIgnoreUrls() { return ignoreUrls; }
    public void setIgnoreUrls(List<String> ignoreUrls) { this.ignoreUrls = ignoreUrls; }
    public boolean isApiPermissionCheck() { return apiPermissionCheck; }
    public void setApiPermissionCheck(boolean apiPermissionCheck) { this.apiPermissionCheck = apiPermissionCheck; }
    public boolean isAllowConcurrentLogin() { return allowConcurrentLogin; }
    public void setAllowConcurrentLogin(boolean allowConcurrentLogin) { this.allowConcurrentLogin = allowConcurrentLogin; }
}

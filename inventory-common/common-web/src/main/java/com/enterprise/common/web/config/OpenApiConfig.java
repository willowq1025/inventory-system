package com.enterprise.common.web.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Knife4j / OpenAPI 3 文档，支持在文档页直接携带 Bearer Token 调试。 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI inventoryOpenAPI() {
        final String scheme = "Authorization";
        return new OpenAPI()
                .info(new Info()
                        .title("企业级进销存 / 库存管理系统 API")
                        .version("1.0.0")
                        .description("""
                                RBAC（用户-角色-菜单-接口权限）· 数据权限 · 统一响应与异常 · 审计日志
                                · 缓存一致性 · 幂等与并发控制 · Excel 导入导出 · 定时任务 · 通知
                                \n认证：登录 /api/auth/login 获取 accessToken，后续请求头携带 Authorization: Bearer <token>
                                \n幂等：写接口可携带 Idempotency-Key 请求头防止重复提交
                                """)
                        .contact(new Contact().name("Enterprise Team").email("dev@example.com")))
                .addSecurityItem(new SecurityRequirement().addList(scheme))
                .components(new Components().addSecuritySchemes(scheme,
                        new SecurityScheme()
                                .name(scheme)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)));
    }
}

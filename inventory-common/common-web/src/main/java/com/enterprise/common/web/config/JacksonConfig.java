package com.enterprise.common.web.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 关键：雪花 ID 是 19 位 Long，超过 JS Number.MAX_SAFE_INTEGER，
 * 全局将 Long（包装类型）序列化为字符串，避免前端精度丢失。
 * 数值型字段请使用基本类型 long / int 或 BigDecimal，不受影响。
 * 以 Module Bean 形式注册，不会覆盖 Spring Boot 自动装配的 JavaTimeModule 等模块。
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Module longToStringModule() {
        SimpleModule module = new SimpleModule("long-to-string");
        module.addSerializer(Long.class, ToStringSerializer.instance);
        return module;
    }
}

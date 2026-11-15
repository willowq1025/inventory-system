package com.enterprise.common.core.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * 独立于 Spring 容器的 JSON 工具（用于幂等快照、Webhook 签名、日志脱敏等场景）。
 *
 * <p>必须和 MVC 的 JacksonConfig 保持同样的 Long -> String 策略：
 * 雪花 ID 是 19 位，超过 JS Number.MAX_SAFE_INTEGER。幂等回放会用本工具把首次响应
 * 序列化成快照再反序列化，如果这里把 Long 写成数字，回放结果会变成 Number 导致前端精度丢失，
 * 且首次响应（字符串）与回放响应（数字）类型不一致。
 */
public final class JsonUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .registerModule(new SimpleModule("long-to-string")
                    .addSerializer(Long.class, ToStringSerializer.instance))
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private JsonUtils() {}

    public static ObjectMapper mapper() { return MAPPER; }

    public static String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            throw new IllegalArgumentException("JSON 序列化失败", e);
        }
    }

    public static <T> T parse(String json, Class<T> clazz) {
        if (json == null || json.isBlank()) return null;
        try {
            return MAPPER.readValue(json, clazz);
        } catch (Exception e) {
            throw new IllegalArgumentException("JSON 反序列化失败", e);
        }
    }

    public static <T> T parse(String json, TypeReference<T> type) {
        if (json == null || json.isBlank()) return null;
        try {
            return MAPPER.readValue(json, type);
        } catch (Exception e) {
            throw new IllegalArgumentException("JSON 反序列化失败", e);
        }
    }
}

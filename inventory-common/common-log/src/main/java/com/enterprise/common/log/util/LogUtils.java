package com.enterprise.common.log.util;

import com.enterprise.common.core.util.JsonUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Set;

/** 日志工具：IP 提取、UserAgent 解析、敏感字段脱敏、长文本截断。 */
public final class LogUtils {

    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "password", "oldPassword", "newPassword", "confirmPassword",
            "token", "accessToken", "secret", "idCard", "privateKey");
    private static final int MAX_LENGTH = 4000;

    private LogUtils() {}

    public static HttpServletRequest requestOrNull() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            return attrs.getRequest();
        }
        return null;
    }

    public static String clientIp(HttpServletRequest request) {
        if (request == null) return "";
        String[] headers = {"X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP"};
        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }

    public static String userAgent(HttpServletRequest request) {
        return request == null ? "" : truncate(request.getHeader("User-Agent"), 500);
    }

    public static String safeJson(Object obj) {
        if (obj == null) return null;
        try {
            String json = JsonUtils.toJson(obj);
            for (String key : SENSITIVE_KEYS) {
                json = json.replaceAll("(\"" + key + "\"\\s*:\\s*)\"[^\"]*\"", "$1\"******\"");
            }
            return truncate(json, MAX_LENGTH);
        } catch (Exception e) {
            return "[序列化失败] " + obj.getClass().getSimpleName();
        }
    }

    public static String truncate(String text, int max) {
        if (text == null) return null;
        return text.length() <= max ? text : text.substring(0, max) + "...(truncated)";
    }

    /** 跳过 File/Multipart/Servlet 等不适合序列化的参数 */
    public static Object[] serializableArgs(Object[] args) {
        if (args == null) return new Object[0];
        return java.util.Arrays.stream(args)
                .filter(a -> a == null || !(a instanceof jakarta.servlet.ServletRequest)
                        && !(a instanceof jakarta.servlet.ServletResponse)
                        && !(a instanceof org.springframework.web.multipart.MultipartFile))
                .toArray();
    }

    public static boolean isSensitive(String key) { return SENSITIVE_KEYS.contains(key); }
}

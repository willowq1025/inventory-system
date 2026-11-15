package com.enterprise.common.notify.handler;

import com.enterprise.common.core.util.JsonUtils;
import com.enterprise.common.notify.config.NotifyProperties;
import com.enterprise.common.notify.model.NotificationEvent;
import com.enterprise.common.notify.model.NotifyChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Webhook 通知：HmacSHA256 签名（X-Signature / X-Timestamp），
 * 接收方可据此验签防伪造；失败记录日志但不阻断业务，可按策略加重试。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookNotificationHandler {

    private final NotifyProperties properties;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3)).build();

    @Async
    @EventListener
    public void onEvent(NotificationEvent event) {
        if (!properties.isWebhookEnabled() || !event.channel(NotifyChannel.WEBHOOK)) return;
        String url = properties.getWebhookUrl();
        if (url == null || url.isBlank()) return;
        try {
            long timestamp = System.currentTimeMillis() / 1000;
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("event", "notification");
            payload.put("timestamp", timestamp);
            payload.put("title", event.getTitle());
            payload.put("content", event.getContent());
            payload.put("level", event.getLevel());
            payload.put("noticeType", event.getNoticeType());
            payload.put("extra", event.getExtra());
            String body = JsonUtils.toJson(payload);
            String signature = hmacSha256(timestamp + "." + body, properties.getWebhookSecret());

            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(properties.getWebhookTimeout())
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .header("X-Timestamp", String.valueOf(timestamp))
                    .header("X-Signature", signature)
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                log.warn("[notify] webhook 返回异常 status={} body={}", response.statusCode(), response.body());
            }
        } catch (Exception e) {
            log.error("[notify] webhook 发送失败 url={}", url, e);
        }
    }

    private String hmacSha256(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }
}

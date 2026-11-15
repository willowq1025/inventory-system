package com.enterprise.common.notify.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "inventory.notify")
public class NotifyProperties {

    private boolean inAppEnabled = true;
    private boolean emailEnabled = false;
    private boolean webhookEnabled = false;
    /** 默认收件人（未显式指定收件人时使用） */
    private List<String> defaultEmails = new ArrayList<>();
    private String from = "noreply@inventory.local";
    private String webhookUrl = "";
    private String webhookSecret = "change-me";
    private Duration webhookTimeout = Duration.ofSeconds(5);

    public boolean isInAppEnabled() { return inAppEnabled; }
    public void setInAppEnabled(boolean inAppEnabled) { this.inAppEnabled = inAppEnabled; }
    public boolean isEmailEnabled() { return emailEnabled; }
    public void setEmailEnabled(boolean emailEnabled) { this.emailEnabled = emailEnabled; }
    public boolean isWebhookEnabled() { return webhookEnabled; }
    public void setWebhookEnabled(boolean webhookEnabled) { this.webhookEnabled = webhookEnabled; }
    public List<String> getDefaultEmails() { return defaultEmails; }
    public void setDefaultEmails(List<String> defaultEmails) { this.defaultEmails = defaultEmails; }
    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }
    public String getWebhookUrl() { return webhookUrl; }
    public void setWebhookUrl(String webhookUrl) { this.webhookUrl = webhookUrl; }
    public String getWebhookSecret() { return webhookSecret; }
    public void setWebhookSecret(String webhookSecret) { this.webhookSecret = webhookSecret; }
    public Duration getWebhookTimeout() { return webhookTimeout; }
    public void setWebhookTimeout(Duration webhookTimeout) { this.webhookTimeout = webhookTimeout; }
}

package com.enterprise.common.notify.handler;

import com.enterprise.common.notify.config.NotifyProperties;
import com.enterprise.common.notify.model.NotificationEvent;
import com.enterprise.common.notify.model.NotifyChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.event.EventListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

/** 邮件通知：仅在开启且配置了 SMTP 时发送。 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationHandler {

    private final NotifyProperties properties;
    private final ObjectProvider<JavaMailSender> mailSender;

    @Async
    @EventListener
    public void onEvent(NotificationEvent event) {
        if (!properties.isEmailEnabled() || !event.channel(NotifyChannel.EMAIL)) return;
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            log.warn("[notify] 邮件已开启但未配置 spring.mail.host，跳过发送");
            return;
        }
        List<String> recipients = event.getToEmails().isEmpty()
                ? properties.getDefaultEmails() : event.getToEmails();
        if (recipients.isEmpty()) return;
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(properties.getFrom());
            message.setTo(recipients.toArray(String[]::new));
            message.setSubject("[进销存系统] " + event.getTitle());
            message.setText(event.getContent() + "\n\n时间：" + event.getOccurTime());
            sender.send(message);
        } catch (Exception e) {
            log.error("[notify] 邮件发送失败 title={}", event.getTitle(), e);
        }
    }
}

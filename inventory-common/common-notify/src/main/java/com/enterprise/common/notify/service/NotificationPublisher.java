package com.enterprise.common.notify.service;

import com.enterprise.common.notify.model.NotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

/** 通知发布入口：业务模块只依赖该类，不关心具体通道。 */
@Service
@RequiredArgsConstructor
public class NotificationPublisher {

    private final ApplicationEventPublisher publisher;

    public void publish(NotificationEvent event) { publisher.publishEvent(event); }

    public void notifyUsers(String title, String content, int level, List<Long> userIds) {
        publish(NotificationEvent.builder()
                .title(title).content(content).level(level)
                .noticeType(1).targetType(2).targetUserIds(userIds)
                .build());
    }

    public void warnAll(String title, String content) {
        publish(NotificationEvent.builder()
                .title(title).content(content).level(3)
                .noticeType(3).targetType(1)
                .channels(java.util.EnumSet.allOf(com.enterprise.common.notify.model.NotifyChannel.class))
                .build());
    }
}

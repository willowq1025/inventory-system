package com.enterprise.common.notify.handler;

import com.enterprise.common.notify.config.NotifyProperties;
import com.enterprise.common.notify.model.NotificationEvent;
import com.enterprise.common.notify.model.NotifyChannel;
import com.enterprise.common.notify.spi.InAppMessageStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** 站内信：写 sys_notice 并按目标人群生成阅读状态记录。 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InAppNotificationHandler {

    private final NotifyProperties properties;
    private final Optional<InAppMessageStore> store;

    @Async
    @EventListener
    public void onEvent(NotificationEvent event) {
        if (!properties.isInAppEnabled() || !event.channel(NotifyChannel.IN_APP)) return;
        if (store.isEmpty()) {
            log.debug("[notify] 未配置站内信存储实现，跳过");
            return;
        }
        try {
            store.get().save(event);
        } catch (Exception e) {
            log.error("[notify] 站内信写入失败 title={}", event.getTitle(), e);
        }
    }
}

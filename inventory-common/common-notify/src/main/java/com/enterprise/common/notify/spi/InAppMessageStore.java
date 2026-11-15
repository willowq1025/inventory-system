package com.enterprise.common.notify.spi;

import com.enterprise.common.notify.model.NotificationEvent;

/** 站内信落库 SPI，module-system 实现（sys_notice + sys_notice_read）。 */
public interface InAppMessageStore {
    void save(NotificationEvent event);
}

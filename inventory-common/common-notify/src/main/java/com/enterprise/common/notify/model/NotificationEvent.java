package com.enterprise.common.notify.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 通知事件（Spring 应用事件，异步广播给站内信 / 邮件 / Webhook 三个通道）。
 */
@Data
@Builder
public class NotificationEvent {

    private String title;
    private String content;
    /** 1通知 2公告 3预警 */
    @Builder.Default
    private int noticeType = 1;
    /** 1普通 2重要 3紧急 */
    @Builder.Default
    private int level = 1;
    /** 1全体 2指定用户 3指定角色 */
    @Builder.Default
    private int targetType = 1;
    @Builder.Default
    private List<Long> targetUserIds = new ArrayList<>();
    @Builder.Default
    private List<Long> targetRoleIds = new ArrayList<>();
    @Builder.Default
    private List<String> toEmails = new ArrayList<>();
    @Builder.Default
    private Set<NotifyChannel> channels = EnumSet.of(NotifyChannel.IN_APP);
    @Builder.Default
    private Map<String, Object> extra = new HashMap<>();
    @Builder.Default
    private LocalDateTime occurTime = LocalDateTime.now();

    public boolean channel(NotifyChannel channel) {
        return channels != null && channels.contains(channel);
    }
}

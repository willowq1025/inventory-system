package com.enterprise.module.system.spi;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.enterprise.common.notify.model.NotificationEvent;
import com.enterprise.common.notify.spi.InAppMessageStore;
import com.enterprise.common.security.util.SecurityUtils;
import com.enterprise.module.system.entity.*;
import com.enterprise.module.system.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/** 站内信落库：公告主体 + 按目标人群展开的阅读状态记录。 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InAppMessageStoreImpl implements InAppMessageStore {

    private final SysNoticeMapper noticeMapper;
    private final SysNoticeReadMapper readMapper;
    private final SysUserRoleMapper userRoleMapper;

    @Async
    @Override
    public void save(NotificationEvent event) {
        try {
            SysNotice notice = new SysNotice();
            notice.setTitle(event.getTitle());
            notice.setContent(event.getContent());
            notice.setNoticeType(event.getNoticeType());
            notice.setLevel(event.getLevel());
            notice.setTargetType(event.getTargetType());
            notice.setTargetIds(event.getTargetUserIds() == null ? ""
                    : event.getTargetUserIds().stream().map(String::valueOf).collect(Collectors.joining(",")));
            notice.setSenderId(SecurityUtils.userId());
            noticeMapper.insert(notice);

            for (Long userId : resolveTargets(event)) {
                SysNoticeRead read = new SysNoticeRead();
                read.setId(IdWorker.getId());
                read.setNoticeId(notice.getId());
                read.setUserId(userId);
                read.setReadFlag(0);
                readMapper.insert(read);
            }
        } catch (Exception e) {
            log.error("[notify] 站内信落库失败 title={}", event.getTitle(), e);
        }
    }

    private Set<Long> resolveTargets(NotificationEvent event) {
        if (event.getTargetType() == 1) return Set.of();   // 全体：查询时直接可见，阅读时补记录
        Set<Long> users = new HashSet<>();
        if (event.getTargetUserIds() != null) users.addAll(event.getTargetUserIds());
        if (event.getTargetType() == 3 && event.getTargetRoleIds() != null
                && !event.getTargetRoleIds().isEmpty()) {
            users.addAll(userRoleMapper.selectList(Wrappers.lambdaQuery(SysUserRole.class)
                            .in(SysUserRole::getRoleId, event.getTargetRoleIds()))
                    .stream().map(SysUserRole::getUserId).toList());
        }
        return users;
    }
}

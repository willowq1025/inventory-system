package com.enterprise.module.system.service;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enterprise.common.core.PageResult;
import com.enterprise.common.notify.model.NotificationEvent;
import com.enterprise.common.notify.service.NotificationPublisher;
import com.enterprise.common.security.util.SecurityUtils;
import com.enterprise.module.system.dto.NoticeCmd;
import com.enterprise.module.system.dto.NoticeQuery;
import com.enterprise.module.system.entity.SysNotice;
import com.enterprise.module.system.mapper.SysNoticeMapper;
import com.enterprise.module.system.mapper.SysNoticeReadMapper;
import com.enterprise.module.system.vo.NoticeVO;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SysNoticeService extends ServiceImpl<SysNoticeMapper, SysNotice> {

    private final SysNoticeReadMapper readMapper;
    private final NotificationPublisher notificationPublisher;

    public PageResult<SysNotice> page(NoticeQuery q) {
        var w = Wrappers.lambdaQuery(SysNotice.class)
                .like(StrUtil.isNotBlank(q.getTitle()), SysNotice::getTitle, q.getTitle())
                .eq(q.getNoticeType() != null, SysNotice::getNoticeType, q.getNoticeType())
                .eq(q.getLevel() != null, SysNotice::getLevel, q.getLevel())
                .ge(q.getStartTime() != null, SysNotice::getCreateTime, q.getStartTime())
                .le(q.getEndTime() != null, SysNotice::getCreateTime, q.getEndTime());
        String orderBy = q.safeOrderBy();
        if (orderBy == null) w.orderByDesc(SysNotice::getCreateTime);
        else w.last("ORDER BY " + orderBy + (q.isAsc() ? " ASC" : " DESC"));
        var page = baseMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()), w);
        return PageResult.of(page.getTotal(), page.getRecords(), page.getCurrent(), page.getSize());
    }

    public List<NoticeVO> mine(Long userId) {
        Set<Long> readIds = new HashSet<>(readMapper.selectReadIds(userId));
        return baseMapper.selectMine(userId).stream()
                .map(n -> NoticeVO.from(n, readIds.contains(n.getId())))
                .toList();
    }

    public long unreadCount(Long userId) {
        return mine(userId).stream().filter(n -> !n.isRead()).count();
    }

    public void markRead(Long noticeId, Long userId) {
        readMapper.markRead(IdWorker.getId(), noticeId, userId);
    }

    public void markAllRead(Long userId) {
        for (NoticeVO vo : mine(userId)) {
            if (!vo.isRead()) markRead(vo.getId(), userId);
        }
    }

    /** 发布通知：通过事件总线异步分发到站内信 / 邮件 / Webhook */
    public void publish(NoticeCmd cmd) {
        NotificationEvent.NotificationEventBuilder builder = NotificationEvent.builder()
                .title(cmd.getTitle())
                .content(cmd.getContent())
                .noticeType(cmd.getNoticeType())
                .level(cmd.getLevel())
                .targetType(cmd.getTargetType())
                .targetUserIds(cmd.getTargetUserIds() == null ? new ArrayList<>() : cmd.getTargetUserIds())
                .targetRoleIds(cmd.getTargetRoleIds() == null ? new ArrayList<>() : cmd.getTargetRoleIds());
        if (cmd.getChannels() != null && !cmd.getChannels().isEmpty()) builder.channels(cmd.getChannels());
        notificationPublisher.publish(builder.build());
    }

    public Long currentUserId() { return SecurityUtils.userId(); }
}

package com.enterprise.module.system.vo;

import com.enterprise.module.system.entity.SysNotice;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NoticeVO {

    private Long id;
    private String title;
    private String content;
    private Integer noticeType;
    private Integer level;
    private Integer targetType;
    private LocalDateTime createTime;
    private boolean read;

    public static NoticeVO from(SysNotice n, boolean read) {
        NoticeVO vo = new NoticeVO();
        vo.setId(n.getId());
        vo.setTitle(n.getTitle());
        vo.setContent(n.getContent());
        vo.setNoticeType(n.getNoticeType());
        vo.setLevel(n.getLevel());
        vo.setTargetType(n.getTargetType());
        vo.setCreateTime(n.getCreateTime());
        vo.setRead(read);
        return vo;
    }
}

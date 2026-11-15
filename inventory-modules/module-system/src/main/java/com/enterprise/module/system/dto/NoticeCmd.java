package com.enterprise.module.system.dto;

import com.enterprise.common.notify.model.NotifyChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class NoticeCmd {

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    @NotNull(message = "通知类型不能为空")
    private Integer noticeType = 1;

    private Integer level = 1;
    private Integer targetType = 1;
    private List<Long> targetUserIds;
    private List<Long> targetRoleIds;
    private Set<NotifyChannel> channels;
}

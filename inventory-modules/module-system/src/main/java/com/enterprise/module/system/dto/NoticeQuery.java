package com.enterprise.module.system.dto;

import com.enterprise.common.core.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
public class NoticeQuery extends PageQuery {

    private String title;
    private Integer noticeType;
    private Integer level;

    @Override
    protected Set<String> allowedOrderColumns() {
        return Set.of("id", "create_time", "level");
    }
}

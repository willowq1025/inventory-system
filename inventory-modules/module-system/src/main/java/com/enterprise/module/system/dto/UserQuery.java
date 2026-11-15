package com.enterprise.module.system.dto;

import com.enterprise.common.core.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserQuery extends PageQuery {

    private String username;
    private String nickname;
    private String phone;
    private Long deptId;
    private Integer status;

    @Override
    protected Set<String> allowedOrderColumns() {
        return Set.of("id", "create_time", "update_time", "username", "sort");
    }
}

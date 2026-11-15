package com.enterprise.module.system.dto;

import com.enterprise.common.core.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
public class RoleQuery extends PageQuery {

    private String roleName;
    private String roleKey;
    private Integer status;

    @Override
    protected Set<String> allowedOrderColumns() {
        return Set.of("id", "create_time", "sort");
    }
}

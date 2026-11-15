package com.enterprise.module.inventory.dto;

import com.enterprise.common.core.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
public class BizSupplierQuery extends PageQuery {

    /** 状态 */
    private Integer status;

    @Override
    protected Set<String> allowedOrderColumns() {
        return Set.of("create_time", "id", "status");
    }
}

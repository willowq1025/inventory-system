package com.enterprise.module.inventory.dto;

import com.enterprise.common.core.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
public class BizCategoryQuery extends PageQuery {

    /** 父分类 */
    private Long parentId;

    @Override
    protected Set<String> allowedOrderColumns() {
        return Set.of("create_time", "id", "parent_id");
    }
}

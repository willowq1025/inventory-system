package com.enterprise.module.inventory.dto;

import com.enterprise.common.core.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
public class StockQuery extends PageQuery {

    private Long productId;
    private Long warehouseId;
    /** 只看低于安全库存的品项 */
    private Boolean lowStockOnly;

    @Override
    protected Set<String> allowedOrderColumns() {
        return Set.of("quantity", "available_quantity", "update_time");
    }
}

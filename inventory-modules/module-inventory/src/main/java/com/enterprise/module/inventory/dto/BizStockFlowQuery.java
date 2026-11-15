package com.enterprise.module.inventory.dto;

import com.enterprise.common.core.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
public class BizStockFlowQuery extends PageQuery {

    /** 商品 */
    private Long productId;

    /** 仓库 */
    private Long warehouseId;

    /** 业务类型 */
    private String bizType;

    @Override
    protected Set<String> allowedOrderColumns() {
        return Set.of("biz_type", "id", "oper_time", "product_id", "warehouse_id");
    }
}

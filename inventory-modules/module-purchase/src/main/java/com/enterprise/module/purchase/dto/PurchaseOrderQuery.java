package com.enterprise.module.purchase.dto;

import com.enterprise.common.core.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
public class PurchaseOrderQuery extends PageQuery {

    private String orderNo;
    private Long supplierId;
    private Long warehouseId;
    private Integer status;

    @Override
    protected Set<String> allowedOrderColumns() {
        return Set.of("id", "create_time", "total_amount", "order_no");
    }
}

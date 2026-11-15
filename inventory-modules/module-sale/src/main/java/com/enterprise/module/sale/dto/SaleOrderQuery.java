package com.enterprise.module.sale.dto;

import com.enterprise.common.core.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
public class SaleOrderQuery extends PageQuery {

    private String orderNo;
    private Long customerId;
    private Long warehouseId;
    private Integer status;

    @Override
    protected Set<String> allowedOrderColumns() {
        return Set.of("id", "create_time", "total_amount", "order_no");
    }
}

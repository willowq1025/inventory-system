package com.enterprise.module.sale.vo;

import com.enterprise.module.sale.entity.BizSaleOrder;
import com.enterprise.module.sale.entity.BizSaleItem;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class SaleOrderVO extends BizSaleOrder {

    private List<BizSaleItem> items;
    private String partnerName;
    private String warehouseName;
}

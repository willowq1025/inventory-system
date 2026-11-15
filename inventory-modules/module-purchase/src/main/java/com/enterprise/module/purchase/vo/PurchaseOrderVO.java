package com.enterprise.module.purchase.vo;

import com.enterprise.module.purchase.entity.BizPurchaseOrder;
import com.enterprise.module.purchase.entity.BizPurchaseItem;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class PurchaseOrderVO extends BizPurchaseOrder {

    private List<BizPurchaseItem> items;
    private String partnerName;
    private String warehouseName;
}

package com.enterprise.module.report.vo;

import lombok.Data;

/** 进销存明细：期初 + 入库 - 出库 = 结存。 */
@Data
public class PssVO {

    private Long productId;
    private String productCode;
    private String productName;
    private String unit;
    private Long warehouseId;
    private String warehouseName;
    private long openingQty;
    private long inQty;
    private long outQty;
    private long closingQty;
}

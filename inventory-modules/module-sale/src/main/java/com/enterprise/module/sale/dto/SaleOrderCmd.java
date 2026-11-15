package com.enterprise.module.sale.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** 销售订单创建命令。 */
@Data
public class SaleOrderCmd {

    private Long id;

    @NotNull(message = "客户不能为空")
    private Long customerId;

    @NotNull(message = "仓库不能为空")
    private Long warehouseId;

    private LocalDate expectDate;
    private String remark;

    @NotEmpty(message = "订单明细不能为空")
    @Valid
    private List<Item> items;

    @Data
    public static class Item {
        @NotNull(message = "商品不能为空")
        private Long productId;

        @NotNull(message = "数量不能为空")
        @Min(value = 1, message = "数量必须大于 0")
        private Long quantity;

        @NotNull(message = "单价不能为空")
        @DecimalMin(value = "0.00", message = "单价不能为负")
        private BigDecimal unitPrice;
    }
}

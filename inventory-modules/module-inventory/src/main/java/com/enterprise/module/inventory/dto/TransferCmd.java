package com.enterprise.module.inventory.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TransferCmd {

    @NotNull(message = "调出仓库不能为空")
    private Long fromWarehouseId;

    @NotNull(message = "调入仓库不能为空")
    private Long toWarehouseId;

    @NotNull(message = "商品不能为空")
    private Long productId;

    @NotNull(message = "调拨数量不能为空")
    private Long quantity;

    private String remark;
}

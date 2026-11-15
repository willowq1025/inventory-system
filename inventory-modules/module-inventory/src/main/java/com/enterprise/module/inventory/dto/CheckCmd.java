package com.enterprise.module.inventory.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CheckCmd {

    @NotNull(message = "仓库不能为空")
    private Long warehouseId;

    private LocalDate checkDate;
    private String remark;

    @NotEmpty(message = "盘点明细不能为空")
    @Valid
    private List<Item> items;

    @Data
    public static class Item {
        @NotNull(message = "商品不能为空")
        private Long productId;
        @NotNull(message = "实盘数量不能为空")
        private Long actualQuantity;
        private String remark;
    }
}

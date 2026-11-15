package com.enterprise.module.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 库存变动命令：quantity > 0 入库，< 0 出库。 */
@Data
public class StockChangeCmd {

    @NotNull(message = "商品不能为空")
    private Long productId;

    @NotNull(message = "仓库不能为空")
    private Long warehouseId;

    /** 变动量，可正可负，不能为 0 */
    @NotNull(message = "变动数量不能为空")
    private Long quantity;

    @NotBlank(message = "业务类型不能为空")
    private String bizType;

    private String bizNo;
    private String remark;

    public static StockChangeCmd of(Long productId, Long warehouseId, long delta,
                                    String bizType, String bizNo, String remark) {
        StockChangeCmd cmd = new StockChangeCmd();
        cmd.setProductId(productId);
        cmd.setWarehouseId(warehouseId);
        cmd.setQuantity(delta);
        cmd.setBizType(bizType);
        cmd.setBizNo(bizNo);
        cmd.setRemark(remark);
        return cmd;
    }
}

package com.enterprise.module.inventory.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.enterprise.common.mybatis.domain.BaseEntity;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * biz_inventory_check（biz_inventory_check）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_inventory_check")
public class BizInventoryCheck extends BaseEntity {

    /** 盘点单号 */
    @TableField("check_no")
    @NotBlank(message = "盘点单号不能为空")
    private String checkNo;

    /** 仓库ID */
    @TableField("warehouse_id")
    @NotNull(message = "仓库ID不能为空")
    private Long warehouseId;

    /** 盘点日期 */
    @TableField("check_date")
    private LocalDate checkDate;

    /** 0草稿 1待审核 2已完成 3已取消 */
    @TableField("status")
    private Integer status;

    /** 审核人 */
    @TableField("audit_by")
    private Long auditBy;

    /** 审核时间 */
    @TableField("audit_time")
    private LocalDateTime auditTime;

    /** 备注 */
    @TableField("remark")
    private String remark;
}

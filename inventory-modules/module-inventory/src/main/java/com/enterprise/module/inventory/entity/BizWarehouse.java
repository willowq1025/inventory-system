package com.enterprise.module.inventory.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.enterprise.common.mybatis.domain.BaseEntity;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.NotBlank;

/**
 * biz_warehouse（biz_warehouse）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_warehouse")
public class BizWarehouse extends BaseEntity {

    /** 仓库编码 */
    @TableField("warehouse_code")
    @NotBlank(message = "仓库编码不能为空")
    private String warehouseCode;

    /** 仓库名称 */
    @TableField("warehouse_name")
    @NotBlank(message = "仓库名称不能为空")
    private String warehouseName;

    /** 地址 */
    @TableField("address")
    private String address;

    /** 负责人 */
    @TableField("manager_id")
    private Long managerId;

    /** 电话 */
    @TableField("phone")
    private String phone;

    /** 状态 */
    @TableField("status")
    private Integer status;

    /** 备注 */
    @TableField("remark")
    private String remark;
}

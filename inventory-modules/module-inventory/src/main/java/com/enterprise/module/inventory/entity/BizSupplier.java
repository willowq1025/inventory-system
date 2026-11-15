package com.enterprise.module.inventory.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.enterprise.common.mybatis.domain.BaseEntity;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.NotBlank;

/**
 * biz_supplier（biz_supplier）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_supplier")
public class BizSupplier extends BaseEntity {

    /** 供应商编码 */
    @TableField("supplier_code")
    @NotBlank(message = "供应商编码不能为空")
    private String supplierCode;

    /** 供应商名称 */
    @TableField("supplier_name")
    @NotBlank(message = "供应商名称不能为空")
    private String supplierName;

    /** 联系人 */
    @TableField("contact")
    private String contact;

    /** 电话 */
    @TableField("phone")
    private String phone;

    /** 邮箱 */
    @TableField("email")
    private String email;

    /** 地址 */
    @TableField("address")
    private String address;

    /** 状态 */
    @TableField("status")
    private Integer status;

    /** 备注 */
    @TableField("remark")
    private String remark;
}

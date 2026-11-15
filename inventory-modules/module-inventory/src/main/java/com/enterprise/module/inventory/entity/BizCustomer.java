package com.enterprise.module.inventory.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.enterprise.common.mybatis.domain.BaseEntity;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.NotBlank;

/**
 * biz_customer（biz_customer）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_customer")
public class BizCustomer extends BaseEntity {

    /** 客户编码 */
    @TableField("customer_code")
    @NotBlank(message = "客户编码不能为空")
    private String customerCode;

    /** 客户名称 */
    @TableField("customer_name")
    @NotBlank(message = "客户名称不能为空")
    private String customerName;

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

package com.enterprise.module.inventory.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.enterprise.common.mybatis.domain.BaseEntity;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.NotBlank;

/**
 * biz_category（biz_category）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_category")
public class BizCategory extends BaseEntity {

    /** 父分类 */
    @TableField("parent_id")
    private Long parentId;

    /** 分类名称 */
    @TableField("category_name")
    @NotBlank(message = "分类名称不能为空")
    private String categoryName;

    /** 分类编码 */
    @TableField("category_code")
    @NotBlank(message = "分类编码不能为空")
    private String categoryCode;

    /** 排序 */
    @TableField("sort")
    private Integer sort;

    /** 状态 */
    @TableField("status")
    private Integer status;

    /** 备注 */
    @TableField("remark")
    private String remark;
}

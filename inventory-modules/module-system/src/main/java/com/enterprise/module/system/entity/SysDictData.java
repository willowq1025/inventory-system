package com.enterprise.module.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.enterprise.common.mybatis.domain.BaseEntity;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.NotBlank;

/**
 * sys_dict_data（sys_dict_data）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_data")
public class SysDictData extends BaseEntity {

    /** 字典类型 */
    @TableField("dict_type")
    @NotBlank(message = "字典类型不能为空")
    private String dictType;

    /** 字典标签 */
    @TableField("dict_label")
    @NotBlank(message = "字典标签不能为空")
    private String dictLabel;

    /** 字典值 */
    @TableField("dict_value")
    @NotBlank(message = "字典值不能为空")
    private String dictValue;

    /** 样式 */
    @TableField("css_class")
    private String cssClass;

    /** 表格样式 */
    @TableField("list_class")
    private String listClass;

    /** 是否默认 */
    @TableField("is_default")
    private Integer isDefault;

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

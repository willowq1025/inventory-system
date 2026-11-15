package com.enterprise.module.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.enterprise.common.mybatis.domain.BaseEntity;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.NotBlank;

/**
 * sys_config（sys_config）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_config")
public class SysConfig extends BaseEntity {

    /** 参数名称 */
    @TableField("config_name")
    @NotBlank(message = "参数名称不能为空")
    private String configName;

    /** 参数键 */
    @TableField("config_key")
    @NotBlank(message = "参数键不能为空")
    private String configKey;

    /** 参数值 */
    @TableField("config_value")
    @NotBlank(message = "参数值不能为空")
    private String configValue;

    /** 是否系统内置 */
    @TableField("config_type")
    private Integer configType;

    /** 备注 */
    @TableField("remark")
    private String remark;
}

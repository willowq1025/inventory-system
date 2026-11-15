package com.enterprise.module.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.enterprise.common.mybatis.domain.BaseEntity;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * sys_user（sys_user）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    /** 部门ID */
    @TableField("dept_id")
    private Long deptId;

    /** 登录账号 */
    @TableField("username")
    @NotBlank(message = "登录账号不能为空")
    private String username;

    /** 用户昵称 */
    @TableField("nickname")
    @NotBlank(message = "用户昵称不能为空")
    private String nickname;

    /** BCrypt密码 */
    @TableField("password")
    private String password;

    /** 邮箱 */
    @TableField("email")
    private String email;

    /** 手机号 */
    @TableField("phone")
    private String phone;

    /** 头像 */
    @TableField("avatar")
    private String avatar;

    /** 性别 */
    @TableField("sex")
    private Integer sex;

    /** 状态 */
    @TableField("status")
    private Integer status;

    /** 最后登录IP */
    @TableField("last_login_ip")
    private String lastLoginIp;

    /** 最后登录时间 */
    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;

    /** 备注 */
    @TableField("remark")
    private String remark;
}

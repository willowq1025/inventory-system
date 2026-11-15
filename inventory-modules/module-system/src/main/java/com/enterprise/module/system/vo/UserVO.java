package com.enterprise.module.system.vo;

import com.enterprise.module.system.entity.SysUser;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/** 用户视图：绝不包含 password 字段。 */
@Data
public class UserVO {

    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private String avatar;
    private Long deptId;
    private String deptName;
    private Integer sex;
    private Integer status;
    private LocalDateTime lastLoginTime;
    private LocalDateTime createTime;
    private List<Long> roleIds;
    private List<String> roleNames;

    public static UserVO from(SysUser user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setAvatar(user.getAvatar());
        vo.setDeptId(user.getDeptId());
        vo.setSex(user.getSex());
        vo.setStatus(user.getStatus());
        vo.setLastLoginTime(user.getLastLoginTime());
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }
}

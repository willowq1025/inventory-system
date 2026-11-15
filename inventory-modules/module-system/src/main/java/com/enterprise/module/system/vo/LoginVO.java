package com.enterprise.module.system.vo;

import com.enterprise.common.security.model.LoginToken;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@Builder
public class LoginVO {

    private LoginToken token;
    private UserVO user;
    private Set<String> permissions;
    private Set<String> roles;
    private List<RouteVO> menus;
}

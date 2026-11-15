package com.enterprise.common.security.model;

import com.enterprise.common.core.context.CurrentUser;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * 登录主体：同时是 Spring Security 的 UserDetails 与全系统通用的 CurrentUser。
 * 权限集合随登录一次性加载并缓存到 Redis，避免每请求查库。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser implements UserDetails, CurrentUser {

    private Long userId;
    private String username;
    private String nickname;
    private String password;
    private Long deptId;
    private Integer dataScope;
    private boolean enabled;

    @Builder.Default
    private Set<String> roleKeys = new HashSet<>();
    @Builder.Default
    private Set<String> permissions = new HashSet<>();
    @Builder.Default
    private Set<Long> customDeptIds = new HashSet<>();

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return permissions.stream().map(SimpleGrantedAuthority::new).distinct().toList();
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() { return true; }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() { return true; }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return enabled; }

    @Override
    public Long getUserId() { return userId; }
    @Override
    public String getUsername() { return username; }
    @Override
    public String getNickname() { return nickname; }
    @Override
    public Long getDeptId() { return deptId; }
    @Override
    public Integer getDataScope() { return dataScope; }
    @Override
    public Set<String> getRoleKeys() { return roleKeys; }
    @Override
    public Set<String> getPermissions() { return permissions; }
    @Override
    public Set<Long> getCustomDeptIds() { return customDeptIds; }
}

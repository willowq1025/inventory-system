package com.enterprise.module.system.spi;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.enterprise.common.core.CommonConstant;
import com.enterprise.common.log.model.LoginLogEvent;
import com.enterprise.common.log.spi.LoginLogRecorder;
import com.enterprise.common.security.model.ApiRule;
import com.enterprise.common.security.model.LoginUser;
import com.enterprise.common.security.model.UserCredentials;
import com.enterprise.common.security.service.PermissionCacheService;
import com.enterprise.common.security.spi.UserAuthProvider;
import com.enterprise.module.system.entity.*;
import com.enterprise.module.system.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 认证数据源实现：把 sys_user / sys_role / sys_menu 组装成 LoginUser，
 * 并写入 Redis 权限缓存。菜单或角色变更后由对应 Service 主动失效缓存。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserAuthProviderImpl implements UserAuthProvider {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;
    private final SysRoleDeptMapper roleDeptMapper;
    private final PermissionCacheService permissionCache;
    private final Optional<LoginLogRecorder> loginLogRecorder;

    @Override
    public Optional<UserCredentials> findCredentials(String username) {
        SysUser user = userMapper.selectByUsername(username);
        return Optional.ofNullable(user).map(u -> new UserCredentials(
                u.getId(), u.getUsername(), u.getPassword(),
                Objects.equals(CommonConstant.STATUS_ENABLED, u.getStatus()), u.getDeptId()));
    }

    @Override
    public LoginUser loadLoginUser(Long userId) {
        LoginUser cached = permissionCache.getUser(userId);
        if (cached != null) return cached;

        SysUser user = userMapper.selectById(userId);
        if (user == null || Objects.equals(0, user.getDeleted())) return null;

        List<SysRole> roles = roleMapper.selectRolesByUserId(userId);
        Set<String> roleKeys = roles.stream().map(SysRole::getRoleKey).collect(Collectors.toSet());
        Set<String> permissions = new HashSet<>(menuMapper.selectPermissionsByUserId(userId));
        if (roleKeys.contains(CommonConstant.SUPER_ADMIN_ROLE)) {
            permissions = Set.of(CommonConstant.ALL_PERMISSION);
        }
        // 多角色取最宽范围（数值越小范围越大）
        int dataScope = roles.stream()
                .mapToInt(r -> r.getDataScope() == null ? CommonConstant.SCOPE_SELF : r.getDataScope())
                .min().orElse(CommonConstant.SCOPE_SELF);
        Set<Long> customDepts = new HashSet<>();
        for (SysRole role : roles) {
            if (Objects.equals(role.getDataScope(), CommonConstant.SCOPE_CUSTOM)) {
                customDepts.addAll(roleDeptMapper.selectDeptIdsByRoleId(role.getId()));
            }
        }

        LoginUser loginUser = LoginUser.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .password(user.getPassword())
                .deptId(user.getDeptId())
                .dataScope(dataScope)
                .enabled(Objects.equals(CommonConstant.STATUS_ENABLED, user.getStatus()))
                .roleKeys(roleKeys)
                .permissions(permissions)
                .customDeptIds(customDepts)
                .build();
        permissionCache.cacheUser(loginUser);
        return loginUser;
    }

    @Override
    public List<ApiRule> listApiRules() {
        List<ApiRule> cached = permissionCache.getApiRules();
        if (cached != null) return cached;
        List<ApiRule> rules = menuMapper.selectApiRules().stream()
                .map(m -> new ApiRule(m.getApiPath(),
                        m.getApiMethod() == null || m.getApiMethod().isBlank() ? "*" : m.getApiMethod(),
                        m.getPermission()))
                .toList();
        permissionCache.cacheApiRules(rules);
        return rules;
    }

    @Override
    public void onLoginSuccess(Long userId, String username, String ip, boolean success, String message) {
        if (success) {
            SysUser update = new SysUser();
            update.setId(userId);
            update.setLastLoginIp(ip);
            update.setLastLoginTime(java.time.LocalDateTime.now());
            userMapper.updateById(update);
        }
        // 登录日志由 LoginLogRecorder 异步写入，避免拖慢登录链路
        LoginLogEvent event = new LoginLogEvent(
                org.slf4j.MDC.get(CommonConstant.TRACE_ID), username, ip, "", "",
                success ? 1 : 0, message, java.time.LocalDateTime.now());
        loginLogRecorder.ifPresent(recorder -> recorder.record(event));
    }
}

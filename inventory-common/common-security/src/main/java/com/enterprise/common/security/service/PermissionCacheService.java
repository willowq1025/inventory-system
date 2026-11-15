package com.enterprise.common.security.service;

import com.enterprise.common.core.CacheKey;
import com.enterprise.common.redis.service.CacheService;
import com.enterprise.common.security.jwt.JwtTokenProvider;
import com.enterprise.common.security.model.ApiRule;
import com.enterprise.common.security.model.LoginUser;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

/**
 * 权限缓存：用户权限集合、接口规则集中缓存。
 * 一致性策略：角色/菜单/用户变更 → 由 module-system 在事务提交后调用 evict* 清理，
 * 既不出现「改了权限仍可访问」，也不出现长时间权限空洞。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionCacheService {

    private final CacheService cacheService;
    private final JwtTokenProvider jwtTokenProvider;

    private String userKey(Long userId) { return CacheKey.PERM_USER + userId; }

    public void cacheUser(LoginUser user) {
        if (user == null || user.getUserId() == null) return;
        cacheService.set(userKey(user.getUserId()), user,
                Duration.ofSeconds(jwtTokenProvider.expiresInSeconds()));
    }

    public LoginUser getUser(Long userId) {
        return cacheService.get(userKey(userId), LoginUser.class);
    }

    public void evictUser(Long userId) {
        if (userId != null) cacheService.evict(userKey(userId));
    }

    /** 角色授权变更：清理该角色下所有用户（实现方传入用户 ID 列表）+ 接口规则 */
    public void evictUsers(List<Long> userIds) {
        if (userIds == null) return;
        userIds.forEach(this::evictUser);
    }

    public void evictAllUsers() {
        cacheService.evictByPrefix(CacheKey.PERM_USER);
    }

    /** 菜单 / 接口权限点变更 */
    public void evictApiRules() { cacheService.evict(CacheKey.API_RULES); }

    public List<ApiRule> getApiRules() {
        return cacheService.get(CacheKey.API_RULES, new TypeReference<>() {});
    }

    public void cacheApiRules(List<ApiRule> rules) {
        cacheService.set(CacheKey.API_RULES, rules, Duration.ofHours(2));
    }
}

package com.enterprise.module.system.service;

import com.enterprise.common.core.BizException;
import com.enterprise.common.core.ResultCode;
import com.enterprise.common.log.util.LogUtils;
import com.enterprise.common.mybatis.datascope.IgnoreDataScope;
import com.enterprise.common.security.jwt.JwtTokenProvider;
import com.enterprise.common.security.model.LoginToken;
import com.enterprise.common.security.model.LoginUser;
import com.enterprise.common.security.model.UserCredentials;
import com.enterprise.common.security.service.PermissionCacheService;
import com.enterprise.common.security.spi.UserAuthProvider;
import com.enterprise.common.security.util.SecurityUtils;
import com.enterprise.module.system.dto.LoginCmd;
import com.enterprise.module.system.entity.SysMenu;
import com.enterprise.module.system.mapper.SysMenuMapper;
import com.enterprise.module.system.vo.LoginVO;
import com.enterprise.module.system.vo.RouteVO;
import com.enterprise.module.system.vo.UserVO;
import com.enterprise.module.system.mapper.SysUserMapper;
import com.enterprise.module.system.entity.SysUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

/** 登录 / 登出 / 当前用户信息与动态路由。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserAuthProvider userAuthProvider;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final PermissionCacheService permissionCache;
    private final SysUserMapper userMapper;
    private final SysMenuMapper menuMapper;

    @IgnoreDataScope
    public LoginVO login(LoginCmd cmd) {
        String ip = LogUtils.clientIp(LogUtils.requestOrNull());
        UserCredentials credentials = userAuthProvider.findCredentials(cmd.getUsername())
                .orElseThrow(() -> {
                    userAuthProvider.onLoginSuccess(null, cmd.getUsername(), ip, false, "账号不存在");
                    return new BizException(ResultCode.LOGIN_ERROR);
                });
        if (!credentials.enabled()) {
            userAuthProvider.onLoginSuccess(credentials.userId(), cmd.getUsername(), ip, false, "账号已停用");
            throw new BizException(ResultCode.USER_DISABLED);
        }
        if (!passwordEncoder.matches(cmd.getPassword(), credentials.password())) {
            userAuthProvider.onLoginSuccess(credentials.userId(), cmd.getUsername(), ip, false, "密码错误");
            throw new BizException(ResultCode.LOGIN_ERROR);
        }

        // 每次登录重新加载权限，保证角色/菜单变更即时生效
        permissionCache.evictUser(credentials.userId());
        LoginUser user = userAuthProvider.loadLoginUser(credentials.userId());
        if (user == null) throw new BizException(ResultCode.LOGIN_ERROR);
        String token = jwtTokenProvider.createToken(user);
        permissionCache.cacheUser(user);
        userAuthProvider.onLoginSuccess(credentials.userId(), cmd.getUsername(), ip, true, "登录成功");

        SysUser dbUser = userMapper.selectById(credentials.userId());
        UserVO userVO = UserVO.from(dbUser);
        return LoginVO.builder()
                .token(new LoginToken(token, "Bearer", jwtTokenProvider.expiresInSeconds(),
                        user.getUserId(), user.getNickname()))
                .user(userVO)
                .permissions(user.getPermissions())
                .roles(user.getRoleKeys())
                .menus(buildRoutes(user.getUserId()))
                .build();
    }

    public LoginVO info() {
        LoginUser user = SecurityUtils.getLoginUser();
        if (user == null) throw new BizException(ResultCode.UNAUTHORIZED);
        SysUser dbUser = userMapper.selectById(user.getUserId());
        return LoginVO.builder()
                .user(UserVO.from(dbUser))
                .permissions(user.getPermissions())
                .roles(user.getRoleKeys())
                .menus(buildRoutes(user.getUserId()))
                .build();
    }

    public void logout() {
        Long userId = SecurityUtils.userId();
        if (userId != null) permissionCache.evictUser(userId);
    }

    /** 由 sys_menu 生成前端动态路由树 */
    public List<RouteVO> buildRoutes(Long userId) {
        List<SysMenu> menus = menuMapper.selectMenusByUserId(userId);
        Map<Long, RouteVO> nodes = new LinkedHashMap<>();
        for (SysMenu m : menus) {
            RouteVO route = new RouteVO();
            route.setId(m.getId());
            route.setPath(m.getPath());
            route.setName(m.getRouteName());
            route.setComponent(m.getComponent());
            Map<String, Object> meta = new HashMap<>();
            meta.put("title", m.getMenuName());
            meta.put("icon", m.getIcon());
            meta.put("permission", m.getPermission());
            meta.put("hidden", m.getVisible() != null && m.getVisible() == 0);
            route.setMeta(meta);
            nodes.put(m.getId(), route);
        }
        List<RouteVO> roots = new ArrayList<>();
        for (SysMenu m : menus) {
            RouteVO current = nodes.get(m.getId());
            RouteVO parent = nodes.get(m.getParentId());
            if (parent != null && !Objects.equals(parent.getId(), current.getId())) {
                parent.getChildren().add(current);
            } else {
                roots.add(current);
            }
        }
        return roots;
    }
}

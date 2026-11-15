package com.enterprise.common.security.spi;

import com.enterprise.common.security.model.ApiRule;
import com.enterprise.common.security.model.LoginUser;
import com.enterprise.common.security.model.UserCredentials;

import java.util.List;
import java.util.Optional;

/**
 * 认证数据源 SPI。common-security 只定义契约，由 module-system 基于 MyBatis-Plus 实现，
 * 从而保证「安全层不反向依赖业务持久层」。
 */
public interface UserAuthProvider {

    Optional<UserCredentials> findCredentials(String username);

    /** 加载用户 + 角色 + 菜单/接口权限 + 数据权限范围（结果应由实现方缓存） */
    LoginUser loadLoginUser(Long userId);

    /** 全部已登记的接口权限规则（实现方应缓存，菜单变更时失效） */
    List<ApiRule> listApiRules();

    /** 登录成功回调（更新最后登录信息、写登录日志） */
    void onLoginSuccess(Long userId, String username, String ip, boolean success, String message);
}

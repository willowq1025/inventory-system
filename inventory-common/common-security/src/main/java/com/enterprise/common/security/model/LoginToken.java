package com.enterprise.common.security.model;

/** 登录令牌响应。 */
public record LoginToken(String accessToken, String tokenType, long expiresIn, Long userId, String nickname) {}

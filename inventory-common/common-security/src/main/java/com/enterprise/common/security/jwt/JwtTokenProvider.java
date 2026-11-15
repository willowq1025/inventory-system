package com.enterprise.common.security.jwt;

import com.enterprise.common.security.config.SecurityProperties;
import com.enterprise.common.security.model.LoginUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/** JWT 生成 / 解析（jjwt 0.12.x，HS256）。 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final SecurityProperties properties;

    public JwtTokenProvider(SecurityProperties properties) {
        this.properties = properties;
        String secret = properties.getJwt().getSecret();
        if (!StringUtils.hasText(secret) || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("inventory.security.jwt.secret 长度必须 >= 32 字节");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(LoginUser user) {
        Instant now = Instant.now();
        Instant exp = now.plus(properties.getJwt().getAccessTokenValidity());
        return Jwts.builder()
                .subject(String.valueOf(user.getUserId()))
                .issuer(properties.getJwt().getIssuer())
                .claim("username", user.getUsername())
                .claim("deptId", user.getDeptId())
                .claim("jti", UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public Claims parse(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(properties.getJwt().getIssuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getUserId(String token) {
        return Long.valueOf(parse(token).getSubject());
    }

    public long expiresInSeconds() {
        return properties.getJwt().getAccessTokenValidity().toSeconds();
    }
}

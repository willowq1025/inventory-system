package com.enterprise.inventory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

/** 验证 V2 初始化脚本中的管理员密码哈希可被 Spring Security 正确校验。 */
class PasswordEncoderTest {

    private static final String INIT_HASH = "$2a$10$rd4XatbmKsrFyaygIEIqueVvH0czYGbVZASiSD4lXcy8moS1Tvi0S";

    @Test
    @DisplayName("admin / Admin@123 初始密码校验通过")
    void initialPasswordMatches() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
        assertThat(encoder.matches("Admin@123", INIT_HASH)).isTrue();
        assertThat(encoder.matches("wrong-password", INIT_HASH)).isFalse();
    }

    @Test
    @DisplayName("BCrypt 编码结果每次不同但均可校验")
    void encodeIsSalted() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
        String a = encoder.encode("Admin@123");
        String b = encoder.encode("Admin@123");
        assertThat(a).isNotEqualTo(b);
        assertThat(encoder.matches("Admin@123", a)).isTrue();
    }
}

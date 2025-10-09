package com.stdev.smartmealtable.api.config;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 비밀번호 암호화 설정
 * Spring Security 없이 BCrypt 라이브러리만 사용
 */
@Configuration
public class PasswordConfig {

    /**
     * BCrypt 해시 비용 (10 = 2^10 iterations)
     * 값이 클수록 안전하지만 느림 (범위: 4-31, 권장: 10-12)
     */
    private static final int BCRYPT_COST = 12;

    /**
     * 비밀번호 암호화
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new PasswordEncoder() {
            @Override
            public String encode(String rawPassword) {
                return BCrypt.withDefaults()
                        .hashToString(BCRYPT_COST, rawPassword.toCharArray());
            }

            @Override
            public boolean matches(String rawPassword, String hashedPassword) {
                BCrypt.Result result = BCrypt.verifyer()
                        .verify(rawPassword.toCharArray(), hashedPassword);
                return result.verified;
            }
        };
    }

    /**
     * 비밀번호 인코더 인터페이스
     */
    public interface PasswordEncoder {
        String encode(String rawPassword);
        boolean matches(String rawPassword, String hashedPassword);
    }
}

package com.stdev.smartmealtable.api.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import at.favre.lib.crypto.bcrypt.BCrypt;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 토큰 설정
 * Spring Security 없이 JWT 라이브러리만 사용
 */
@Configuration
public class JwtConfig {
    
    @Value("${jwt.secret}")
    private String jwtSecretKey;
    
    /**
     * JWT 서명용 SecretKey Bean
     */
    @Bean
    public SecretKey jwtSecretKey() {
        return Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * JWT Parser Bean
     */
    @Bean
    public JwtParser jwtParser(SecretKey jwtSecretKey) {
        return Jwts.parser()
                .verifyWith(jwtSecretKey)
                .build();
    }
    
    /**
     * JWT 토큰 생성 및 검증 클래스
     */
    @Component
    public static class JwtTokenProvider {
        
        private final SecretKey secretKey;
        private final JwtParser jwtParser;
        private final long accessTokenExpiryTime;
        private final long refreshTokenExpiryTime;
        
        public JwtTokenProvider(
                SecretKey secretKey,
                JwtParser jwtParser,
                @Value("${jwt.access-token-expiry}") long accessTokenExpiryTime,
                @Value("${jwt.refresh-token-expiry}") long refreshTokenExpiryTime) {
            this.secretKey = secretKey;
            this.jwtParser = jwtParser;
            this.accessTokenExpiryTime = accessTokenExpiryTime;
            this.refreshTokenExpiryTime = refreshTokenExpiryTime;
        }
        
        /**
         * Access Token 생성
         */
        public String generateAccessToken(String email) {
            long now = System.currentTimeMillis();
            
            return Jwts.builder()
                    .claim("type", "access")
                    .claim("email", email)
                    .issuedAt(new Date(now))
                    .expiration(new Date(now + accessTokenExpiryTime))
                    .signWith(secretKey)
                    .compact();
        }
        
        /**
         * Refresh Token 생성
         */
        public String generateRefreshToken(String email) {
            long now = System.currentTimeMillis();
            
            return Jwts.builder()
                    .claim("type", "refresh")
                    .claim("email", email)
                    .issuedAt(new Date(now))
                    .expiration(new Date(now + refreshTokenExpiryTime))
                    .signWith(secretKey)
                    .compact();
        }
        
        /**
         * 토큰에서 이메일 추출
         */
        public String getEmailFromToken(String token) {
            Claims claims = jwtParser.parseSignedClaims(token).getPayload();
            return claims.get("email", String.class);
        }
        
        /**
         * 토큰에서 토큰 타입 추출
         */
        public String getTypeFromToken(String token) {
            Claims claims = jwtParser.parseSignedClaims(token).getPayload();
            return claims.get("type", String.class);
        }
        
        /**
         * 토큰 유효성 검증
         */
        public boolean isTokenValid(String token) {
            try {
                jwtParser.parseSignedClaims(token);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        
        /**
         * 토큰 만료 시간 체크
         */
        public boolean isTokenExpired(String token) {
            try {
                Claims claims = jwtParser.parseSignedClaims(token).getPayload();
                return claims.getExpiration().before(new Date());
            } catch (Exception e) {
                return true;
            }
        }
    }
    
    /**
     * BCrypt 암호화 유틸리티
     */
    @Component
    public static class PasswordEncoder {
        
        /**
         * 비밀번호 암호화
         */
        public static String encode(String rawPassword) {
            return BCrypt.withDefaults().hashToString(12, rawPassword.toCharArray());
        }
        
        /**
         * 비밀번호 검증
         */
        public static boolean matches(String rawPassword, String encodedPassword) {
            return BCrypt.verifyer().verify(rawPassword.toCharArray(), encodedPassword).verified;
        }
    }
}
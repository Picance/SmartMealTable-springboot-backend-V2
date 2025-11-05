package com.stdev.smartmealtable.support.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 토큰 생성 및 파싱 유틸리티
 * STATELESS한 JWT 방식으로 인증 처리
 */
@Component
@Slf4j
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long validityInMilliseconds;

    public JwtTokenProvider(
            @Value("${jwt.secret:smartmealtable-secret-key-for-jwt-token-generation-minimum-256-bits}") String secret,
            @Value("${jwt.validity-in-milliseconds:3600000}") long validityInMilliseconds
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.validityInMilliseconds = validityInMilliseconds;
    }

    /**
     * JWT 토큰 생성
     * @param memberId 회원 ID
     * @return JWT 토큰 문자열
     */
    public String createToken(Long memberId) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setSubject(String.valueOf(memberId))
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * JWT 토큰에서 memberId 추출
     * @param token JWT 토큰
     * @return memberId
     * @throws io.jsonwebtoken.JwtException 토큰 파싱 실패 시
     */
    public Long extractMemberId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String subject = claims.getSubject();
        return Long.parseLong(subject);
    }

    /**
     * Authorization 헤더에서 Bearer 토큰 추출
     * @param authorizationHeader Authorization 헤더 값
     * @return JWT 토큰 문자열 (Bearer 제외), 유효하지 않으면 null
     */
    public String extractTokenFromHeader(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7).trim();
            // 빈 문자열 체크
            return token.isEmpty() ? null : token;
        }
        return null;
    }

    /**
     * JWT 토큰 유효성 검증
     * @param token JWT 토큰
     * @return 유효하면 true
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            log.warn("JWT 토큰 검증 실패: {}", e.getMessage());
            return false;
        }
    }
}

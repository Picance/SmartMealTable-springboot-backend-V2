package com.stdev.smartmealtable.client.auth.oauth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * OAuth 토큰 응답 DTO
 */
@Getter
@NoArgsConstructor
public class OAuthTokenResponse {
    
    private String accessToken;
    private String refreshToken;
    private Integer expiresIn;
    private String tokenType;
    private String idToken;
    
    public OAuthTokenResponse(String accessToken, String refreshToken, Integer expiresIn, String tokenType, String idToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.tokenType = tokenType;
        this.idToken = idToken;
    }
    
    /**
     * 토큰 만료 시간 계산
     */
    public LocalDateTime calculateExpiresAt() {
        if (expiresIn == null) {
            return LocalDateTime.now().plusHours(1); // 기본 1시간
        }
        return LocalDateTime.now().plusSeconds(expiresIn);
    }
}

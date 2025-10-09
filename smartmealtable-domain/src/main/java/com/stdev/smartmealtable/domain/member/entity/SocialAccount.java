package com.stdev.smartmealtable.domain.member.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 소셜 계정 도메인 엔티티
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SocialAccount {

    private Long socialAccountId;
    private Long memberAuthenticationId;  // 논리 FK
    private SocialProvider provider;
    private String providerId;
    private String tokenType;
    private String accessToken;
    private String refreshToken;
    private LocalDateTime expiresAt;  // 비즈니스 필드

    public static SocialAccount create(
            Long memberAuthenticationId,
            SocialProvider provider,
            String providerId,
            String accessToken,
            String refreshToken,
            String tokenType,
            LocalDateTime expiresAt
    ) {
        SocialAccount account = new SocialAccount();
        account.memberAuthenticationId = memberAuthenticationId;
        account.provider = provider;
        account.providerId = providerId;
        account.accessToken = accessToken;
        account.refreshToken = refreshToken;
        account.tokenType = tokenType;
        account.expiresAt = expiresAt;
        return account;
    }

    // 재구성 팩토리 메서드 (Storage에서 사용)
    public static SocialAccount reconstitute(
            Long socialAccountId,
            Long memberAuthenticationId,
            SocialProvider provider,
            String providerId,
            String accessToken,
            String refreshToken,
            String tokenType,
            LocalDateTime expiresAt
    ) {
        SocialAccount account = new SocialAccount();
        account.socialAccountId = socialAccountId;
        account.memberAuthenticationId = memberAuthenticationId;
        account.provider = provider;
        account.providerId = providerId;
        account.accessToken = accessToken;
        account.refreshToken = refreshToken;
        account.tokenType = tokenType;
        account.expiresAt = expiresAt;
        return account;
    }

    // 도메인 로직: 토큰 갱신
    public void updateToken(String newAccessToken, String newRefreshToken, LocalDateTime newExpiresAt) {
        this.accessToken = newAccessToken;
        if (newRefreshToken != null) {
            this.refreshToken = newRefreshToken;
        }
        this.expiresAt = newExpiresAt;
    }

    // 도메인 로직: 토큰 만료 여부
    public boolean isTokenExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
}

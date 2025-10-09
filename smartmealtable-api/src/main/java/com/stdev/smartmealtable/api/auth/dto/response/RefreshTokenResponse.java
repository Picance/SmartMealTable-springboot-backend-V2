package com.stdev.smartmealtable.api.auth.dto.response;

/**
 * 토큰 재발급 응답 DTO
 */
public record RefreshTokenResponse(
        String accessToken,
        String refreshToken
) {
}
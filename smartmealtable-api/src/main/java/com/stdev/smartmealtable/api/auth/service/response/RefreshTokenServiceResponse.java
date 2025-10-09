package com.stdev.smartmealtable.api.auth.service.response;

/**
 * 토큰 재발급 서비스 응답 DTO
 */
public record RefreshTokenServiceResponse(
        String accessToken,
        String refreshToken
) {
}
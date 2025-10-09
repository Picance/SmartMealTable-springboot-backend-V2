package com.stdev.smartmealtable.api.auth.service.request;

/**
 * 토큰 재발급 서비스 요청 DTO
 */
public record RefreshTokenServiceRequest(
        String refreshToken
) {
    public static RefreshTokenServiceRequest from(String refreshToken) {
        return new RefreshTokenServiceRequest(refreshToken);
    }
}
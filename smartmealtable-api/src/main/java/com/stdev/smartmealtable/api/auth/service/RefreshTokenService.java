package com.stdev.smartmealtable.api.auth.service;

import com.stdev.smartmealtable.api.auth.service.request.RefreshTokenServiceRequest;
import com.stdev.smartmealtable.api.auth.service.response.RefreshTokenServiceResponse;
import com.stdev.smartmealtable.support.jwt.JwtTokenProvider;
import com.stdev.smartmealtable.core.exception.AuthenticationException;
import com.stdev.smartmealtable.core.error.ErrorType;
import org.springframework.stereotype.Service;

/**
 * 토큰 재발급 서비스
 */
@Service
public class RefreshTokenService {

    private final JwtTokenProvider jwtTokenProvider;

    public RefreshTokenService(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * 리프레시 토큰으로 새로운 액세스 토큰과 리프레시 토큰 발급
     */
    public RefreshTokenServiceResponse refreshToken(RefreshTokenServiceRequest request) {
        String refreshToken = request.refreshToken();

        try {
            // 토큰 유효성 검증
            if (!jwtTokenProvider.validateToken(refreshToken)) {
                throw new AuthenticationException(ErrorType.INVALID_REFRESH_TOKEN);
            }

            // 토큰에서 memberId 추출
            Long memberId = jwtTokenProvider.extractMemberId(refreshToken);

            // 새로운 액세스 토큰 및 리프레시 토큰 생성
            String newAccessToken = jwtTokenProvider.createToken(memberId);
            String newRefreshToken = jwtTokenProvider.createToken(memberId);

            return new RefreshTokenServiceResponse(newAccessToken, newRefreshToken);

        } catch (Exception e) {
            // JWT 파싱 실패 등의 경우
            throw new AuthenticationException(ErrorType.INVALID_REFRESH_TOKEN);
        }
    }
}
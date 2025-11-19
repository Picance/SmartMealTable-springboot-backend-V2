package com.stdev.smartmealtable.api.common.auth;

import com.stdev.smartmealtable.support.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * 인증이 필수가 아닌 엔드포인트에서 JWT 토큰을 선택적으로 파싱하기 위한 헬퍼
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OptionalAuthenticatedUserProvider {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Authorization 헤더가 존재하고 유효한 경우 memberId 반환
     */
    public Optional<Long> extractMemberId(HttpServletRequest request) {
        if (request == null) {
            return Optional.empty();
        }

        String authorizationHeader = request.getHeader("Authorization");
        if (!StringUtils.hasText(authorizationHeader)) {
            return Optional.empty();
        }

        String token = jwtTokenProvider.extractTokenFromHeader(authorizationHeader);
        if (!StringUtils.hasText(token)) {
            return Optional.empty();
        }

        try {
            if (!jwtTokenProvider.validateToken(token)) {
                return Optional.empty();
            }
            Long memberId = jwtTokenProvider.extractMemberId(token);
            return Optional.ofNullable(memberId);
        } catch (Exception ex) {
            log.debug("선택적 인증 토큰 파싱 실패: {}", ex.getMessage());
            return Optional.empty();
        }
    }
}

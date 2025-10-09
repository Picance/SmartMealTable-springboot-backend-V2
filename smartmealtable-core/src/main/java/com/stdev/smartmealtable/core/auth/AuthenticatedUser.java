package com.stdev.smartmealtable.core.auth;

/**
 * JWT 인증된 사용자 정보
 * ArgumentResolver를 통해 JWT 토큰에서 추출된 인증 정보를 담는 객체
 * 
 * 향후 확장 가능: roles, permissions, email 등 추가 필드
 */
public record AuthenticatedUser(
        Long memberId
        // 향후 확장 예시:
        // String email,
        // Set<String> roles,
        // String nickname
) {
    /**
     * memberId로 AuthenticatedUser 생성
     */
    public static AuthenticatedUser of(Long memberId) {
        return new AuthenticatedUser(memberId);
    }
}

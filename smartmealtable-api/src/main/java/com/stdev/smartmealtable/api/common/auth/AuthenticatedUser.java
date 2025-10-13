package com.stdev.smartmealtable.api.common.auth;

/**
 * 인증된 사용자 정보
 * ArgumentResolver를 통해 주입됩니다.
 */
public record AuthenticatedUser(
        Long memberId,
        String email
) {
    public static AuthenticatedUser of(Long memberId, String email) {
        return new AuthenticatedUser(memberId, email);
    }
}

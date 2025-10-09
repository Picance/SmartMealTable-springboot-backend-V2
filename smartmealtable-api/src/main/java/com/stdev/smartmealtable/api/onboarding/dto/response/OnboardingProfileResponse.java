package com.stdev.smartmealtable.api.onboarding.dto.response;

/**
 * 온보딩 - 프로필 설정 응답 DTO
 */
public record OnboardingProfileResponse(
        Long memberId,
        String nickname,
        GroupInfo group
) {
    public record GroupInfo(
            Long groupId,
            String name,
            String type,
            String address
    ) {
    }
}

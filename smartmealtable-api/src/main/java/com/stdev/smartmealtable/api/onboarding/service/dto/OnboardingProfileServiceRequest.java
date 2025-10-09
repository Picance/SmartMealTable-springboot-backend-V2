package com.stdev.smartmealtable.api.onboarding.service.dto;

/**
 * 온보딩 프로필 설정 Service Request DTO
 */
public record OnboardingProfileServiceRequest(
        Long memberId,  // JWT에서 추출한 인증된 회원 ID
        String nickname,
        Long groupId
) {
}

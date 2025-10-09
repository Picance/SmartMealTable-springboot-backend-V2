package com.stdev.smartmealtable.api.onboarding.dto;

import com.stdev.smartmealtable.domain.member.entity.GroupType;

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
            GroupType type,
            String address
    ) {
    }
}

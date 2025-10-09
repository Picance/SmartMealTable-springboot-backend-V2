package com.stdev.smartmealtable.api.onboarding.service.dto;

import com.stdev.smartmealtable.domain.member.entity.GroupType;

/**
 * 온보딩 프로필 설정 Service Response DTO
 */
public record OnboardingProfileServiceResponse(
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

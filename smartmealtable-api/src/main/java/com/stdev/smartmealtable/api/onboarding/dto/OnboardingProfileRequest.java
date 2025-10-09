package com.stdev.smartmealtable.api.onboarding.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 온보딩 - 프로필 설정 요청 DTO
 */
public record OnboardingProfileRequest(

        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(min = 2, max = 50, message = "닉네임은 2-50자 사이여야 합니다.")
        String nickname,

        @NotNull(message = "소속 그룹은 필수입니다.")
        Long groupId
) {
}

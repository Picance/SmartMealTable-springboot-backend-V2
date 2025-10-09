package com.stdev.smartmealtable.api.onboarding.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 온보딩 - 프로필 설정 요청 DTO
 */
public record OnboardingProfileRequest(
        
        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(min = 2, max = 20, message = "닉네임은 2~20자 이내여야 합니다.")
        String nickname,
        
        @NotNull(message = "그룹 ID는 필수입니다.")
        Long groupId
) {
}

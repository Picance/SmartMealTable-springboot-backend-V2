package com.stdev.smartmealtable.api.onboarding.controller.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 개별 음식 선호도 저장 API Request
 */
public record SaveFoodPreferencesRequest(
        @NotNull(message = "선호 음식 목록은 필수입니다.")
        @Size(max = 50, message = "최대 50개의 음식만 선택할 수 있습니다.")
        List<Long> preferredFoodIds
) {
}

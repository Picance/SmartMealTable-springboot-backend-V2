package com.stdev.smartmealtable.api.onboarding.service.dto;

import java.util.List;

/**
 * 개별 음식 선호도 저장 Service Request
 */
public record SaveFoodPreferencesServiceRequest(
        List<Long> preferredFoodIds
) {
}

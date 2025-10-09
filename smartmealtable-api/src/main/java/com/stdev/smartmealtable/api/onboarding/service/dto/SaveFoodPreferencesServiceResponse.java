package com.stdev.smartmealtable.api.onboarding.service.dto;

import java.util.List;

/**
 * 개별 음식 선호도 저장 Service Response
 */
public record SaveFoodPreferencesServiceResponse(
        int savedCount,
        List<PreferredFoodInfo> preferredFoods,
        String message
) {
    /**
     * 선호 음식 정보
     */
    public record PreferredFoodInfo(
            Long foodId,
            String foodName,
            String categoryName,
            String imageUrl
    ) {
    }
}

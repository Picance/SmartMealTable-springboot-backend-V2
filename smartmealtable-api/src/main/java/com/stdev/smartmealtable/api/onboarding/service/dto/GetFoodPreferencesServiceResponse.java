package com.stdev.smartmealtable.api.onboarding.service.dto;

import java.util.List;

/**
 * 온보딩 - 개별 음식 선호도 조회 Service Response
 */
public record GetFoodPreferencesServiceResponse(
        int totalCount,
        List<Long> preferredFoodIds,
        List<PreferredFoodInfo> preferredFoods
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

package com.stdev.smartmealtable.api.onboarding.controller.dto;

import com.stdev.smartmealtable.api.onboarding.service.dto.GetFoodPreferencesServiceResponse;

import java.util.List;

/**
 * 온보딩 - 개별 음식 선호도 조회 Controller Response
 */
public record GetFoodPreferencesResponse(
        int totalCount,
        List<Long> preferredFoodIds,
        List<PreferredFoodInfo> preferredFoods
) {

    public static GetFoodPreferencesResponse from(GetFoodPreferencesServiceResponse serviceResponse) {
        List<PreferredFoodInfo> preferredFoodInfos = serviceResponse.preferredFoods().stream()
                .map(info -> new PreferredFoodInfo(
                        info.foodId(),
                        info.foodName(),
                        info.categoryName(),
                        info.imageUrl()
                ))
                .toList();

        return new GetFoodPreferencesResponse(
                serviceResponse.totalCount(),
                serviceResponse.preferredFoodIds(),
                preferredFoodInfos
        );
    }

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

package com.stdev.smartmealtable.api.onboarding.controller.dto;

import com.stdev.smartmealtable.api.onboarding.service.dto.SaveFoodPreferencesServiceResponse;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 개별 음식 선호도 저장 API Response
 */
public record SaveFoodPreferencesResponse(
        int savedCount,
        List<PreferredFoodInfo> preferredFoods,
        String message
) {
    /**
     * Service Response → Controller Response 변환
     */
    public static SaveFoodPreferencesResponse from(SaveFoodPreferencesServiceResponse serviceResponse) {
        List<PreferredFoodInfo> preferredFoodInfos = serviceResponse.preferredFoods().stream()
                .map(f -> new PreferredFoodInfo(
                        f.foodId(),
                        f.foodName(),
                        f.categoryName(),
                        f.imageUrl()
                ))
                .collect(Collectors.toList());

        return new SaveFoodPreferencesResponse(
                serviceResponse.savedCount(),
                preferredFoodInfos,
                serviceResponse.message()
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

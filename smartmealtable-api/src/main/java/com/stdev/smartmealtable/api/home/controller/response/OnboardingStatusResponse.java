package com.stdev.smartmealtable.api.home.controller.response;

import com.stdev.smartmealtable.api.home.service.dto.OnboardingStatusServiceResponse;

/**
 * 온보딩 상태 조회 응답 DTO (Controller Layer)
 */
public record OnboardingStatusResponse(
        Boolean isOnboardingComplete,
        Boolean hasSelectedRecommendationType,
        Boolean hasConfirmedMonthlyBudget,
        String currentMonth,
        Boolean showRecommendationTypeModal,
        Boolean showMonthlyBudgetModal
) {
    
    /**
     * Service Response를 Controller Response로 변환
     */
    public static OnboardingStatusResponse from(OnboardingStatusServiceResponse serviceResponse) {
        return new OnboardingStatusResponse(
                serviceResponse.isOnboardingComplete(),
                serviceResponse.hasSelectedRecommendationType(),
                serviceResponse.hasConfirmedMonthlyBudget(),
                serviceResponse.currentMonth(),
                serviceResponse.showRecommendationTypeModal(),
                serviceResponse.showMonthlyBudgetModal()
        );
    }
}

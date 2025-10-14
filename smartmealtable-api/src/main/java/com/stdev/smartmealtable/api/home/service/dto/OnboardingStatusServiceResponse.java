package com.stdev.smartmealtable.api.home.service.dto;

/**
 * 온보딩 상태 조회 응답 DTO (Service Layer)
 *
 * @param isOnboardingComplete        온보딩 완료 여부
 * @param hasSelectedRecommendationType 추천 유형 선택 여부
 * @param hasConfirmedMonthlyBudget   월별 예산 확인 여부
 * @param currentMonth                 현재 연월 (YYYY-MM)
 * @param showRecommendationTypeModal 추천 유형 선택 모달 표시 여부
 * @param showMonthlyBudgetModal       월별 예산 확인 모달 표시 여부
 */
public record OnboardingStatusServiceResponse(
        Boolean isOnboardingComplete,
        Boolean hasSelectedRecommendationType,
        Boolean hasConfirmedMonthlyBudget,
        String currentMonth,
        Boolean showRecommendationTypeModal,
        Boolean showMonthlyBudgetModal
) {
    
    /**
     * 팩토리 메서드
     */
    public static OnboardingStatusServiceResponse of(
            Boolean isOnboardingComplete,
            Boolean hasSelectedRecommendationType,
            Boolean hasConfirmedMonthlyBudget,
            String currentMonth,
            Boolean showRecommendationTypeModal,
            Boolean showMonthlyBudgetModal
    ) {
        return new OnboardingStatusServiceResponse(
                isOnboardingComplete,
                hasSelectedRecommendationType,
                hasConfirmedMonthlyBudget,
                currentMonth,
                showRecommendationTypeModal,
                showMonthlyBudgetModal
        );
    }
}

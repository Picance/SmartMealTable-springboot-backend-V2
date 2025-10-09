package com.stdev.smartmealtable.api.onboarding.service.dto;

import com.stdev.smartmealtable.domain.budget.MealType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 예산 설정 Service Response DTO
 */
@Getter
@RequiredArgsConstructor
public class SetBudgetServiceResponse {
    private final Integer monthlyBudget;
    private final Integer dailyBudget;
    private final List<MealBudgetInfo> mealBudgets;

    @Getter
    @RequiredArgsConstructor
    public static class MealBudgetInfo {
        private final MealType mealType;
        private final Integer budget;
    }
}

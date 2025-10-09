package com.stdev.smartmealtable.api.budget.service.dto;

import com.stdev.smartmealtable.domain.budget.MealType;

import java.time.LocalDate;
import java.util.List;

/**
 * 일별 예산 조회 응답 DTO (Service Layer)
 */
public record DailyBudgetQueryServiceResponse(
        LocalDate date,
        Integer totalBudget,
        Integer totalSpent,
        Integer remainingBudget,
        List<MealBudgetInfo> mealBudgets
) {
    public record MealBudgetInfo(
            MealType mealType,
            Integer budget,
            Integer spent,
            Integer remaining
    ) {
    }
}

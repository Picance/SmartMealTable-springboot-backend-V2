package com.stdev.smartmealtable.api.budget.service;

import com.stdev.smartmealtable.domain.expenditure.MealType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 일일 예산 일괄 등록 Service Response DTO
 */
@Getter
@RequiredArgsConstructor
public class BulkCreateDailyBudgetServiceResponse {
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final Integer dailyBudgetCount;
    private final Integer dailyFoodBudget;
    private final List<MealBudgetInfo> mealBudgets;
    private final String message;

    @Getter
    @RequiredArgsConstructor
    public static class MealBudgetInfo {
        private final MealType mealType;
        private final Integer budget;
    }
}

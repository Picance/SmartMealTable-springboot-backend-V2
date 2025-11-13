package com.stdev.smartmealtable.api.budget.service;

import com.stdev.smartmealtable.domain.expenditure.MealType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

/**
 * 일일 예산 일괄 등록 Service Request DTO
 */
@Getter
@RequiredArgsConstructor
public class BulkCreateDailyBudgetServiceRequest {
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final Integer dailyFoodBudget;
    private final Map<MealType, Integer> mealBudgets;
}

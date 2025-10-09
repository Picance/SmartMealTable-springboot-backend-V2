package com.stdev.smartmealtable.api.onboarding.service.dto;

import com.stdev.smartmealtable.domain.budget.MealType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

/**
 * 예산 설정 Service Request DTO
 */
@Getter
@RequiredArgsConstructor
public class SetBudgetServiceRequest {
    private final Integer monthlyBudget;
    private final Integer dailyBudget;
    private final Map<MealType, Integer> mealBudgets;
}

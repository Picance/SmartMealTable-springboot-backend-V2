package com.stdev.smartmealtable.api.budget.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 월별 예산 등록 Service Request DTO
 */
@Getter
@RequiredArgsConstructor
public class CreateMonthlyBudgetServiceRequest {
    private final Integer monthlyFoodBudget;
    private final String budgetMonth;
}

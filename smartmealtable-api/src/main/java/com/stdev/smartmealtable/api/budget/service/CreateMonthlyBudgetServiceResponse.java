package com.stdev.smartmealtable.api.budget.service;

import lombok.Getter;

/**
 * 월별 예산 등록 Service Response DTO
 */
@Getter
public class CreateMonthlyBudgetServiceResponse {
    private final Long monthlyBudgetId;
    private final Integer monthlyFoodBudget;
    private final String budgetMonth;
    private final String message;

    public CreateMonthlyBudgetServiceResponse(Long monthlyBudgetId, Integer monthlyFoodBudget, String budgetMonth) {
        this.monthlyBudgetId = monthlyBudgetId;
        this.monthlyFoodBudget = monthlyFoodBudget;
        this.budgetMonth = budgetMonth;
        this.message = "예산이 성공적으로 등록되었습니다.";
    }
}

package com.stdev.smartmealtable.api.budget.service;

import lombok.Getter;

/**
 * 월별 예산 수정 Service Response DTO
 */
@Getter
public class UpdateBudgetServiceResponse {
    private final Long monthlyBudgetId;
    private final Integer monthlyFoodBudget;
    private final Integer dailyFoodBudget;
    private final String budgetMonth;
    private final String message;

    public UpdateBudgetServiceResponse(Long monthlyBudgetId, Integer monthlyFoodBudget, 
                                        Integer dailyFoodBudget, String budgetMonth) {
        this.monthlyBudgetId = monthlyBudgetId;
        this.monthlyFoodBudget = monthlyFoodBudget;
        this.dailyFoodBudget = dailyFoodBudget;
        this.budgetMonth = budgetMonth;
        this.message = "예산이 성공적으로 수정되었습니다.";
    }
}

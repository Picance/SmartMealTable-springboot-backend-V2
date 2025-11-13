package com.stdev.smartmealtable.api.budget.controller.response;

import com.stdev.smartmealtable.api.budget.service.CreateMonthlyBudgetServiceResponse;
import lombok.Getter;

/**
 * 월별 예산 등록 Response DTO
 */
@Getter
public class CreateMonthlyBudgetResponse {
    private final Long monthlyBudgetId;
    private final Integer monthlyFoodBudget;
    private final String budgetMonth;
    private final String message;

    public CreateMonthlyBudgetResponse(CreateMonthlyBudgetServiceResponse serviceResponse) {
        this.monthlyBudgetId = serviceResponse.getMonthlyBudgetId();
        this.monthlyFoodBudget = serviceResponse.getMonthlyFoodBudget();
        this.budgetMonth = serviceResponse.getBudgetMonth();
        this.message = serviceResponse.getMessage();
    }
}

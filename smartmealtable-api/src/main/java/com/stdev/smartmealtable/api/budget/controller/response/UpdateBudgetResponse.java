package com.stdev.smartmealtable.api.budget.controller.response;

import com.stdev.smartmealtable.api.budget.service.UpdateBudgetServiceResponse;
import lombok.Getter;

/**
 * 월별 예산 수정 Response DTO
 */
@Getter
public class UpdateBudgetResponse {
    private final Long monthlyBudgetId;
    private final Integer monthlyFoodBudget;
    private final Integer dailyFoodBudget;
    private final String budgetMonth;
    private final String message;

    public UpdateBudgetResponse(UpdateBudgetServiceResponse serviceResponse) {
        this.monthlyBudgetId = serviceResponse.getMonthlyBudgetId();
        this.monthlyFoodBudget = serviceResponse.getMonthlyFoodBudget();
        this.dailyFoodBudget = serviceResponse.getDailyFoodBudget();
        this.budgetMonth = serviceResponse.getBudgetMonth();
        this.message = serviceResponse.getMessage();
    }
}

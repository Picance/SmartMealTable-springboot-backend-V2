package com.stdev.smartmealtable.api.budget.service;

import lombok.Getter;

import java.time.LocalDate;

/**
 * 일별 예산 수정 Service Response DTO
 */
@Getter
public class UpdateDailyBudgetServiceResponse {
    private final Long budgetId;
    private final Integer dailyFoodBudget;
    private final LocalDate budgetDate;
    private final Boolean appliedForward;
    private final Integer updatedCount;

    public UpdateDailyBudgetServiceResponse(
            Long budgetId,
            Integer dailyFoodBudget,
            LocalDate budgetDate,
            Boolean appliedForward,
            Integer updatedCount
    ) {
        this.budgetId = budgetId;
        this.dailyFoodBudget = dailyFoodBudget;
        this.budgetDate = budgetDate;
        this.appliedForward = appliedForward;
        this.updatedCount = updatedCount;
    }
}

package com.stdev.smartmealtable.api.budget.service;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 월별 예산 수정 Service Request DTO
 */
@Getter
@NoArgsConstructor
public class UpdateBudgetServiceRequest {
    private Integer monthlyFoodBudget;
    private Integer dailyFoodBudget;

    public UpdateBudgetServiceRequest(Integer monthlyFoodBudget, Integer dailyFoodBudget) {
        this.monthlyFoodBudget = monthlyFoodBudget;
        this.dailyFoodBudget = dailyFoodBudget;
    }
}

package com.stdev.smartmealtable.api.budget.controller.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 월별 예산 수정 Request DTO
 */
@Getter
@NoArgsConstructor
public class UpdateBudgetRequest {

    @NotNull(message = "월별 예산은 필수입니다.")
    @Min(value = 1000, message = "월별 예산은 최소 1,000원 이상이어야 합니다.")
    private Integer monthlyFoodBudget;

    @NotNull(message = "일일 예산은 필수입니다.")
    @Min(value = 100, message = "일일 예산은 최소 100원 이상이어야 합니다.")
    private Integer dailyFoodBudget;

    public UpdateBudgetRequest(Integer monthlyFoodBudget, Integer dailyFoodBudget) {
        this.monthlyFoodBudget = monthlyFoodBudget;
        this.dailyFoodBudget = dailyFoodBudget;
    }
}

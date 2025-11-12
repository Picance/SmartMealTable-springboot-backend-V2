package com.stdev.smartmealtable.api.budget.controller.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 일별 예산 수정 Request DTO
 */
@Getter
@NoArgsConstructor
public class UpdateDailyBudgetRequest {

    @NotNull(message = "일일 예산은 필수입니다.")
    @Min(value = 100, message = "일일 예산은 최소 100원 이상이어야 합니다.")
    private Integer dailyFoodBudget;

    @NotNull(message = "이후 날짜 적용 여부는 필수입니다.")
    private Boolean applyForward;

    public UpdateDailyBudgetRequest(Integer dailyFoodBudget, Boolean applyForward) {
        this.dailyFoodBudget = dailyFoodBudget;
        this.applyForward = applyForward;
    }
}

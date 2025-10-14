package com.stdev.smartmealtable.api.onboarding.controller.dto;

import com.stdev.smartmealtable.api.onboarding.service.dto.SetBudgetServiceRequest;
import com.stdev.smartmealtable.domain.expenditure.MealType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 예산 설정 Request DTO
 */
@Getter
@NoArgsConstructor
public class SetBudgetRequest {

    @NotNull(message = "월별 예산은 필수입니다.")
    @Min(value = 0, message = "월별 예산은 0 이상이어야 합니다.")
    private Integer monthlyBudget;

    @NotNull(message = "일일 예산은 필수입니다.")
    @Min(value = 0, message = "일일 예산은 0 이상이어야 합니다.")
    private Integer dailyBudget;

    @NotNull(message = "식사별 예산은 필수입니다.")
    private Map<MealType, Integer> mealBudgets;

    /**
     * Request DTO → Service DTO 변환
     */
    public SetBudgetServiceRequest toServiceRequest() {
        return new SetBudgetServiceRequest(monthlyBudget, dailyBudget, mealBudgets);
    }
}

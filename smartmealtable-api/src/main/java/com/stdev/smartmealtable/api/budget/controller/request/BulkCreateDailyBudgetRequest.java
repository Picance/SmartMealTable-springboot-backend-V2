package com.stdev.smartmealtable.api.budget.controller.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.stdev.smartmealtable.api.budget.service.BulkCreateDailyBudgetServiceRequest;
import com.stdev.smartmealtable.domain.expenditure.MealType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

/**
 * 일일 예산 일괄 등록 Request DTO
 */
@Getter
@NoArgsConstructor
public class BulkCreateDailyBudgetRequest {

    @NotNull(message = "시작일은 필수입니다.")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @NotNull(message = "종료일은 필수입니다.")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @NotNull(message = "일일 예산은 필수입니다.")
    @Min(value = 100, message = "일일 예산은 최소 100원 이상이어야 합니다.")
    private Integer dailyFoodBudget;

    @NotNull(message = "식사별 예산은 필수입니다.")
    private Map<MealType, @Min(value = 0, message = "식사별 예산은 0 이상이어야 합니다.") Integer> mealBudgets;

    public BulkCreateDailyBudgetServiceRequest toServiceRequest() {
        return new BulkCreateDailyBudgetServiceRequest(startDate, endDate, dailyFoodBudget, mealBudgets);
    }
}

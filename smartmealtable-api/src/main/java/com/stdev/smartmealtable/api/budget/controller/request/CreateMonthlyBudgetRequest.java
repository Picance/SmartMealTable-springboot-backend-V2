package com.stdev.smartmealtable.api.budget.controller.request;

import com.stdev.smartmealtable.api.budget.service.CreateMonthlyBudgetServiceRequest;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

/**
 * 월별 예산 등록 Request DTO
 */
@Getter
@NoArgsConstructor
public class CreateMonthlyBudgetRequest {

    @NotNull(message = "월별 예산은 필수입니다.")
    @Min(value = 1000, message = "월별 예산은 최소 1,000원 이상이어야 합니다.")
    private Integer monthlyFoodBudget;

    @NotBlank(message = "예산 적용 월은 필수입니다.")
    @Length(min = 7, max = 7, message = "예산 적용 월은 YYYY-MM 형식이어야 합니다.")
    private String budgetMonth;

    public CreateMonthlyBudgetRequest(Integer monthlyFoodBudget, String budgetMonth) {
        this.monthlyFoodBudget = monthlyFoodBudget;
        this.budgetMonth = budgetMonth;
    }

    public CreateMonthlyBudgetServiceRequest toServiceRequest() {
        return new CreateMonthlyBudgetServiceRequest(monthlyFoodBudget, budgetMonth);
    }
}

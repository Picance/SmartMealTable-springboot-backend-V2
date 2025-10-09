package com.stdev.smartmealtable.api.budget.controller.response;

import com.stdev.smartmealtable.api.budget.service.UpdateDailyBudgetServiceResponse;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 일별 예산 수정 Response DTO
 */
@Getter
public class UpdateDailyBudgetResponse {
    private final Long budgetId;
    private final Integer dailyFoodBudget;
    private final LocalDate budgetDate;
    private final Boolean appliedForward;
    private final Integer updatedCount;
    private final String message;

    public UpdateDailyBudgetResponse(UpdateDailyBudgetServiceResponse serviceResponse) {
        this.budgetId = serviceResponse.getBudgetId();
        this.dailyFoodBudget = serviceResponse.getDailyFoodBudget();
        this.budgetDate = serviceResponse.getBudgetDate();
        this.appliedForward = serviceResponse.getAppliedForward();
        this.updatedCount = serviceResponse.getUpdatedCount();
        
        if (serviceResponse.getAppliedForward()) {
            this.message = String.format("%s부터 이후 모든 예산이 수정되었습니다. (총 %d개)",
                    serviceResponse.getBudgetDate(), serviceResponse.getUpdatedCount());
        } else {
            this.message = String.format("%s 예산이 수정되었습니다.", serviceResponse.getBudgetDate());
        }
    }
}

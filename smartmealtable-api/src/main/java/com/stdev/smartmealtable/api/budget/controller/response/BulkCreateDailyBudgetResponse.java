package com.stdev.smartmealtable.api.budget.controller.response;

import com.stdev.smartmealtable.api.budget.service.BulkCreateDailyBudgetServiceResponse;
import com.stdev.smartmealtable.domain.expenditure.MealType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 일일 예산 일괄 등록 Response DTO
 */
@Getter
public class BulkCreateDailyBudgetResponse {
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final Integer dailyBudgetCount;
    private final Integer dailyFoodBudget;
    private final List<MealBudgetInfo> mealBudgets;
    private final String message;

    public BulkCreateDailyBudgetResponse(BulkCreateDailyBudgetServiceResponse serviceResponse) {
        this.startDate = serviceResponse.getStartDate();
        this.endDate = serviceResponse.getEndDate();
        this.dailyBudgetCount = serviceResponse.getDailyBudgetCount();
        this.dailyFoodBudget = serviceResponse.getDailyFoodBudget();
        this.mealBudgets = serviceResponse.getMealBudgets().stream()
                .map(info -> new MealBudgetInfo(info.getMealType(), info.getBudget()))
                .collect(Collectors.toList());
        this.message = serviceResponse.getMessage();
    }

    @Getter
    @RequiredArgsConstructor
    public static class MealBudgetInfo {
        private final MealType mealType;
        private final Integer budget;
    }
}

package com.stdev.smartmealtable.api.budget.controller.dto;

import com.stdev.smartmealtable.api.budget.service.dto.DailyBudgetQueryServiceResponse;
import com.stdev.smartmealtable.domain.expenditure.MealType;

import java.time.LocalDate;
import java.util.List;

/**
 * 일별 예산 조회 응답 DTO (Controller Layer)
 */
public record DailyBudgetQueryResponse(
        LocalDate date,
        Integer totalBudget,
        Integer totalSpent,
        Integer remainingBudget,
        List<MealBudgetInfo> mealBudgets
) {
    public record MealBudgetInfo(
            MealType mealType,
            Integer budget,
            Integer spent,
            Integer remaining
    ) {
    }

    public static DailyBudgetQueryResponse from(DailyBudgetQueryServiceResponse response) {
        List<MealBudgetInfo> mealBudgets = response.mealBudgets().stream()
                .map(mb -> new MealBudgetInfo(
                        mb.mealType(),
                        mb.budget(),
                        mb.spent(),
                        mb.remaining()
                ))
                .toList();

        return new DailyBudgetQueryResponse(
                response.date(),
                response.totalBudget(),
                response.totalSpent(),
                response.remainingBudget(),
                mealBudgets
        );
    }
}

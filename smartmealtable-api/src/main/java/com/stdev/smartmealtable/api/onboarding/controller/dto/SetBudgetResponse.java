package com.stdev.smartmealtable.api.onboarding.controller.dto;

import com.stdev.smartmealtable.api.onboarding.service.dto.SetBudgetServiceResponse;
import com.stdev.smartmealtable.domain.expenditure.MealType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 예산 설정 Response DTO
 */
@Getter
@RequiredArgsConstructor
public class SetBudgetResponse {
    private final Integer monthlyBudget;
    private final Integer dailyBudget;
    private final List<MealBudgetInfo> mealBudgets;

    @Getter
    @RequiredArgsConstructor
    public static class MealBudgetInfo {
        private final MealType mealType;
        private final Integer budget;
    }

    /**
     * Service DTO → Response DTO 변환
     */
    public static SetBudgetResponse from(SetBudgetServiceResponse serviceResponse) {
        List<MealBudgetInfo> mealBudgets = serviceResponse.getMealBudgets().stream()
                .map(info -> new MealBudgetInfo(info.getMealType(), info.getBudget()))
                .collect(Collectors.toList());

        return new SetBudgetResponse(
                serviceResponse.getMonthlyBudget(),
                serviceResponse.getDailyBudget(),
                mealBudgets
        );
    }
}

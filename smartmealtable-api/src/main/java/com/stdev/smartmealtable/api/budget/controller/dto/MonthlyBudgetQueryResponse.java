package com.stdev.smartmealtable.api.budget.controller.dto;

import com.stdev.smartmealtable.api.budget.service.dto.MonthlyBudgetQueryServiceResponse;

import java.math.BigDecimal;

/**
 * 월별 예산 조회 응답 DTO (Controller Layer)
 *
 * @param year             연도
 * @param month            월
 * @param totalBudget      해당 월 전체 예산
 * @param totalSpent       해당 월 사용한 금액
 * @param remainingBudget  남은 예산
 * @param utilizationRate  예산 사용률 (%)
 * @param daysRemaining    남은 일수
 */
public record MonthlyBudgetQueryResponse(
        Integer year,
        Integer month,
        Integer totalBudget,
        Integer totalSpent,
        Integer remainingBudget,
        BigDecimal utilizationRate,
        Integer daysRemaining
) {
    public static MonthlyBudgetQueryResponse from(MonthlyBudgetQueryServiceResponse response) {
        return new MonthlyBudgetQueryResponse(
                response.year(),
                response.month(),
                response.totalBudget(),
                response.totalSpent(),
                response.remainingBudget(),
                response.utilizationRate(),
                response.daysRemaining()
        );
    }
}

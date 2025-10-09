package com.stdev.smartmealtable.api.budget.service.dto;

/**
 * 월별 예산 조회 요청 DTO (Service Layer)
 *
 * @param memberId 회원 ID
 * @param year     조회할 연도
 * @param month    조회할 월
 */
public record MonthlyBudgetQueryServiceRequest(
        Long memberId,
        Integer year,
        Integer month
) {
}

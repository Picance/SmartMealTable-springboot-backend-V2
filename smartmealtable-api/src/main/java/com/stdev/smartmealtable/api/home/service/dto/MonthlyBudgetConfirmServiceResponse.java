package com.stdev.smartmealtable.api.home.service.dto;

import com.stdev.smartmealtable.domain.member.entity.MonthlyBudgetConfirmation;

import java.time.LocalDateTime;

/**
 * 월별 예산 확인 처리 응답 DTO (Service Layer)
 *
 * @param year           연도
 * @param month          월 (1-12)
 * @param confirmedAt    확인 시각
 * @param monthlyBudget  월별 예산 금액
 */
public record MonthlyBudgetConfirmServiceResponse(
        Integer year,
        Integer month,
        LocalDateTime confirmedAt,
        Integer monthlyBudget
) {
    
    /**
     * 도메인 객체로부터 Service Response 생성
     */
    public static MonthlyBudgetConfirmServiceResponse of(
            MonthlyBudgetConfirmation confirmation,
            Integer monthlyBudget
    ) {
        return new MonthlyBudgetConfirmServiceResponse(
                confirmation.getYear(),
                confirmation.getMonth(),
                confirmation.getConfirmedAt(),
                monthlyBudget
        );
    }
}

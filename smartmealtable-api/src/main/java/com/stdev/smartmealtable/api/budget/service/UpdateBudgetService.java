package com.stdev.smartmealtable.api.budget.service;

import com.stdev.smartmealtable.domain.budget.MonthlyBudget;
import com.stdev.smartmealtable.domain.budget.service.BudgetDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;

/**
 * 월별 예산 수정 Application Service
 * - 유즈케이스: 월별 예산 및 해당 월의 모든 일일 예산 수정
 * - Orchestration: Domain Service 호출 및 DTO 변환
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UpdateBudgetService {

    private final BudgetDomainService budgetDomainService;

    /**
     * 월별 예산 수정
     * - 월별 예산 금액 업데이트
     * - 해당 월의 모든 일일 예산 금액 업데이트
     */
    public UpdateBudgetServiceResponse updateBudget(Long memberId, UpdateBudgetServiceRequest request) {
        // 현재 월의 예산 수정 (BudgetDomainService)
        String currentMonth = YearMonth.now().toString();

        // 1. 월별 예산 수정
        MonthlyBudget monthlyBudget = budgetDomainService.updateMonthlyBudget(
                memberId,
                currentMonth,
                request.getMonthlyFoodBudget()
        );

        // 2. 일일 예산 일괄 수정
        budgetDomainService.updateDailyBudgetsInMonth(
                memberId,
                currentMonth,
                request.getDailyFoodBudget()
        );

        // 3. 응답 생성
        return new UpdateBudgetServiceResponse(
                monthlyBudget.getMonthlyBudgetId(),
                monthlyBudget.getMonthlyFoodBudget(),
                request.getDailyFoodBudget(),
                monthlyBudget.getBudgetMonth()
        );
    }
}

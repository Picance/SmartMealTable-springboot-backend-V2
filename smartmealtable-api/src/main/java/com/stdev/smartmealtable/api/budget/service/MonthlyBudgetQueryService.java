package com.stdev.smartmealtable.api.budget.service;

import com.stdev.smartmealtable.api.budget.service.dto.MonthlyBudgetQueryServiceRequest;
import com.stdev.smartmealtable.api.budget.service.dto.MonthlyBudgetQueryServiceResponse;
import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.domain.budget.MonthlyBudget;
import com.stdev.smartmealtable.domain.budget.MonthlyBudgetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * 월별 예산 조회 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MonthlyBudgetQueryService {

    private final MonthlyBudgetRepository monthlyBudgetRepository;

    /**
     * 월별 예산 조회
     *
     * @param request 조회 요청 (회원 ID, 연도, 월)
     * @return 월별 예산 정보
     */
    public MonthlyBudgetQueryServiceResponse getMonthlyBudget(MonthlyBudgetQueryServiceRequest request) {
        String budgetMonth = String.format("%04d-%02d", request.year(), request.month());

        MonthlyBudget monthlyBudget = monthlyBudgetRepository.findByMemberIdAndBudgetMonth(
                        request.memberId(), budgetMonth)
                .orElseThrow(() -> new BusinessException(ErrorType.MONTHLY_BUDGET_NOT_FOUND));

        // 남은 일수 계산
        LocalDate today = LocalDate.now();
        YearMonth targetMonth = YearMonth.of(request.year(), request.month());
        LocalDate endOfMonth = targetMonth.atEndOfMonth();
        int daysRemaining = (int) today.until(endOfMonth).getDays();

        // 남은 예산 계산
        Integer remainingBudget = monthlyBudget.getMonthlyFoodBudget() - monthlyBudget.getMonthlyUsedAmount();

        // 예산 사용률 계산 (소수점 둘째 자리까지)
        BigDecimal utilizationRate = BigDecimal.ZERO;
        if (monthlyBudget.getMonthlyFoodBudget() > 0) {
            utilizationRate = BigDecimal.valueOf(monthlyBudget.getMonthlyUsedAmount())
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(monthlyBudget.getMonthlyFoodBudget()), 2, RoundingMode.HALF_UP);
        }

        return new MonthlyBudgetQueryServiceResponse(
                request.year(),
                request.month(),
                monthlyBudget.getMonthlyFoodBudget(),
                monthlyBudget.getMonthlyUsedAmount(),
                remainingBudget,
                utilizationRate,
                daysRemaining
        );
    }
}

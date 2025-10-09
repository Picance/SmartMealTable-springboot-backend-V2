package com.stdev.smartmealtable.api.budget.service;

import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.domain.budget.DailyBudget;
import com.stdev.smartmealtable.domain.budget.DailyBudgetRepository;
import com.stdev.smartmealtable.domain.budget.MonthlyBudget;
import com.stdev.smartmealtable.domain.budget.MonthlyBudgetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * 월별 예산 수정 Service
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UpdateBudgetService {

    private final MonthlyBudgetRepository monthlyBudgetRepository;
    private final DailyBudgetRepository dailyBudgetRepository;

    /**
     * 월별 예산 수정
     * - 월별 예산 금액 업데이트
     * - 해당 월의 모든 일일 예산 금액 업데이트
     */
    public UpdateBudgetServiceResponse updateBudget(Long memberId, UpdateBudgetServiceRequest request) {
        // 현재 월의 예산 조회
        String currentMonth = YearMonth.now().toString();
        MonthlyBudget monthlyBudget = monthlyBudgetRepository.findByMemberIdAndBudgetMonth(memberId, currentMonth)
                .orElseThrow(() -> new BusinessException(ErrorType.MONTHLY_BUDGET_NOT_FOUND));

        // 월별 예산 금액 수정
        monthlyBudget.changeMonthlyFoodBudget(request.getMonthlyFoodBudget());
        monthlyBudgetRepository.save(monthlyBudget);

        // 해당 월의 모든 일일 예산 금액 수정
        LocalDate startOfMonth = YearMonth.parse(currentMonth).atDay(1);
        LocalDate endOfMonth = YearMonth.parse(currentMonth).atEndOfMonth();
        
        List<DailyBudget> dailyBudgets = dailyBudgetRepository.findByMemberIdAndBudgetDateGreaterThanEqual(memberId, startOfMonth);
        
        for (DailyBudget dailyBudget : dailyBudgets) {
            if (!dailyBudget.getBudgetDate().isAfter(endOfMonth)) {
                dailyBudget.changeDailyFoodBudget(request.getDailyFoodBudget());
                dailyBudgetRepository.save(dailyBudget);
            }
        }

        return new UpdateBudgetServiceResponse(
                monthlyBudget.getMonthlyBudgetId(),
                monthlyBudget.getMonthlyFoodBudget(),
                request.getDailyFoodBudget(),
                monthlyBudget.getBudgetMonth()
        );
    }
}

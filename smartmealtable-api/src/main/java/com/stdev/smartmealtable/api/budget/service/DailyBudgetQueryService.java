package com.stdev.smartmealtable.api.budget.service;

import com.stdev.smartmealtable.api.budget.service.dto.DailyBudgetQueryServiceResponse;
import com.stdev.smartmealtable.api.budget.service.dto.DailyBudgetQueryServiceResponse.MealBudgetInfo;
import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.domain.budget.DailyBudget;
import com.stdev.smartmealtable.domain.budget.DailyBudgetRepository;
import com.stdev.smartmealtable.domain.budget.MealBudget;
import com.stdev.smartmealtable.domain.budget.MealBudgetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 일별 예산 조회 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, noRollbackFor = BusinessException.class)
public class DailyBudgetQueryService {

    private final DailyBudgetRepository dailyBudgetRepository;
    private final MealBudgetRepository mealBudgetRepository;

    /**
     * 일별 예산 조회
     *
     * @param memberId 회원 ID
     * @param date     조회 날짜
     * @return 일별 예산 정보 (끼니별 포함)
     */
    public DailyBudgetQueryServiceResponse getDailyBudget(Long memberId, LocalDate date) {
        // 일일 예산 조회
        DailyBudget dailyBudget = dailyBudgetRepository.findByMemberIdAndBudgetDate(memberId, date)
                .orElseThrow(() -> new BusinessException(ErrorType.DAILY_BUDGET_NOT_FOUND));

        // 끼니별 예산 조회
        List<MealBudget> mealBudgets = mealBudgetRepository.findByDailyBudgetId(dailyBudget.getBudgetId());

        // 끼니별 예산 정보 변환
        List<MealBudgetInfo> mealBudgetInfos = mealBudgets.stream()
                .map(mb -> new MealBudgetInfo(
                        mb.getMealType(),
                        mb.getMealBudget(),
                        mb.getUsedAmount(),
                        mb.getMealBudget() - mb.getUsedAmount()
                ))
                .toList();

        // 남은 예산 계산
        Integer remainingBudget = dailyBudget.getDailyFoodBudget() - dailyBudget.getDailyUsedAmount();

        return new DailyBudgetQueryServiceResponse(
                date,
                dailyBudget.getDailyFoodBudget(),
                dailyBudget.getDailyUsedAmount(),
                remainingBudget,
                mealBudgetInfos
        );
    }
}

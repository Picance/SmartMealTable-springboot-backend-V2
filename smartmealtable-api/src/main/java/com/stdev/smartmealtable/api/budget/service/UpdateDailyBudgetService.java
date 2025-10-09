package com.stdev.smartmealtable.api.budget.service;

import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.domain.budget.DailyBudget;
import com.stdev.smartmealtable.domain.budget.DailyBudgetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 일별 예산 수정 Service
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UpdateDailyBudgetService {

    private final DailyBudgetRepository dailyBudgetRepository;

    /**
     * 일별 예산 수정
     * - applyForward가 true이면 해당 날짜부터 미래의 모든 예산 수정
     * - applyForward가 false이면 해당 날짜만 수정
     *
     * @param memberId       회원 ID
     * @param date           수정할 날짜
     * @param newBudget      새 예산 금액
     * @param applyForward   이후 날짜 적용 여부
     * @return 수정된 예산 개수
     */
    public UpdateDailyBudgetServiceResponse updateDailyBudget(
            Long memberId, LocalDate date, Integer newBudget, Boolean applyForward
    ) {
        // 해당 날짜의 예산 조회
        DailyBudget dailyBudget = dailyBudgetRepository.findByMemberIdAndBudgetDate(memberId, date)
                .orElseThrow(() -> new BusinessException(ErrorType.DAILY_BUDGET_NOT_FOUND));

        int updatedCount = 0;

        if (applyForward) {
            // 해당 날짜 이후의 모든 예산 수정
            List<DailyBudget> futureBudgets = dailyBudgetRepository
                    .findByMemberIdAndBudgetDateGreaterThanEqual(memberId, date);

            for (DailyBudget budget : futureBudgets) {
                budget.changeDailyFoodBudget(newBudget);
                dailyBudgetRepository.save(budget);
                updatedCount++;
            }
        } else {
            // 해당 날짜만 수정
            dailyBudget.changeDailyFoodBudget(newBudget);
            dailyBudgetRepository.save(dailyBudget);
            updatedCount = 1;
        }

        return new UpdateDailyBudgetServiceResponse(
                dailyBudget.getBudgetId(),
                newBudget,
                date,
                applyForward,
                updatedCount
        );
    }
}

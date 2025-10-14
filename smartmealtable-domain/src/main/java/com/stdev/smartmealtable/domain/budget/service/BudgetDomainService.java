package com.stdev.smartmealtable.domain.budget.service;

import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.domain.budget.*;
import com.stdev.smartmealtable.domain.expenditure.MealType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

/**
 * 예산 도메인 서비스
 * 예산 생성, 수정 등 핵심 비즈니스 로직을 담당
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BudgetDomainService {

    private final MonthlyBudgetRepository monthlyBudgetRepository;
    private final DailyBudgetRepository dailyBudgetRepository;
    private final MealBudgetRepository mealBudgetRepository;

    /**
     * 온보딩 시 초기 예산 설정
     * - 현재 월의 월별 예산 생성
     * - 오늘 날짜의 일일 예산 생성
     * - 오늘 날짜의 식사별 예산 생성 (아침, 점심, 저녁)
     *
     * @param memberId      회원 ID
     * @param monthlyAmount 월별 예산 금액
     * @param dailyAmount   일일 예산 금액
     * @param mealBudgets   식사별 예산 (MealType → 금액)
     * @return 생성된 월별, 일일, 식사별 예산 객체들
     */
    public BudgetSetupResult setupInitialBudget(
            Long memberId,
            Integer monthlyAmount,
            Integer dailyAmount,
            Map<MealType, Integer> mealBudgets
    ) {
        // 1. 현재 년월 및 날짜 계산
        YearMonth currentMonth = YearMonth.now();
        String budgetMonth = currentMonth.toString(); // "YYYY-MM"
        LocalDate today = LocalDate.now();

        // 2. 월별 예산 생성
        MonthlyBudget monthlyBudget = MonthlyBudget.create(
                memberId,
                monthlyAmount,
                budgetMonth
        );
        monthlyBudget = monthlyBudgetRepository.save(monthlyBudget);

        // 3. 일일 예산 생성
        DailyBudget dailyBudget = DailyBudget.create(
                memberId,
                dailyAmount,
                today
        );
        DailyBudget savedDailyBudget = dailyBudgetRepository.save(dailyBudget);

        // 4. 식사별 예산 생성
        List<MealBudget> createdMealBudgets = mealBudgets.entrySet().stream()
                .map(entry -> {
                    MealBudget mealBudget = MealBudget.create(
                            savedDailyBudget.getBudgetId(),
                            entry.getValue(),
                            entry.getKey(),
                            today
                    );
                    return mealBudgetRepository.save(mealBudget);
                })
                .toList();

        log.info("초기 예산 설정 완료 - memberId: {}, monthly: {}, daily: {}, meals: {}",
                memberId, monthlyAmount, dailyAmount, mealBudgets.size());

        return new BudgetSetupResult(monthlyBudget, savedDailyBudget, createdMealBudgets);
    }

    /**
     * 월별 예산 수정
     *
     * @param memberId 회원 ID
     * @param month 예산 월 (YYYY-MM)
     * @param newAmount 새 월별 예산 금액
     * @return 수정된 월별 예산
     */
    public MonthlyBudget updateMonthlyBudget(Long memberId, String month, Integer newAmount) {
        MonthlyBudget monthlyBudget = monthlyBudgetRepository.findByMemberIdAndBudgetMonth(memberId, month)
                .orElseThrow(() -> new BusinessException(ErrorType.MONTHLY_BUDGET_NOT_FOUND));

        monthlyBudget.changeMonthlyFoodBudget(newAmount);
        return monthlyBudgetRepository.save(monthlyBudget);
    }

    /**
     * 특정 월의 모든 일일 예산 일괄 수정
     *
     * @param memberId 회원 ID
     * @param month 예산 월 (YYYY-MM)
     * @param newAmount 새 일일 예산 금액
     * @return 수정된 일일 예산 리스트
     */
    public List<DailyBudget> updateDailyBudgetsInMonth(Long memberId, String month, Integer newAmount) {
        // 해당 월의 시작일과 종료일 계산
        YearMonth yearMonth = YearMonth.parse(month);
        LocalDate startOfMonth = yearMonth.atDay(1);
        LocalDate endOfMonth = yearMonth.atEndOfMonth();

        // 해당 월의 모든 일일 예산 조회
        List<DailyBudget> dailyBudgets = dailyBudgetRepository.findByMemberIdAndBudgetDateGreaterThanEqual(memberId, startOfMonth);

        // 해당 월의 일일 예산만 필터링하여 수정
        List<DailyBudget> updatedBudgets = dailyBudgets.stream()
                .filter(budget -> !budget.getBudgetDate().isAfter(endOfMonth))
                .peek(budget -> budget.changeDailyFoodBudget(newAmount))
                .map(dailyBudgetRepository::save)
                .toList();

        log.info("일일 예산 일괄 수정 완료 - memberId: {}, month: {}, count: {}, amount: {}",
                memberId, month, updatedBudgets.size(), newAmount);

        return updatedBudgets;
    }

    /**
     * 예산 설정 결과 DTO
     */
    public record BudgetSetupResult(
            MonthlyBudget monthlyBudget,
            DailyBudget dailyBudget,
            List<MealBudget> mealBudgets
    ) {
    }
}

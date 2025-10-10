package com.stdev.smartmealtable.domain.budget.service;

import com.stdev.smartmealtable.domain.budget.*;
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
     * 예산 설정 결과 DTO
     */
    public record BudgetSetupResult(
            MonthlyBudget monthlyBudget,
            DailyBudget dailyBudget,
            List<MealBudget> mealBudgets
    ) {
    }
}

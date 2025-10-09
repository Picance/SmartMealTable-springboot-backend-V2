package com.stdev.smartmealtable.domain.budget;

import java.time.LocalDate;
import java.util.List;

/**
 * 식사 예산 Repository 인터페이스
 */
public interface MealBudgetRepository {
    /**
     * 식사 예산 저장
     */
    MealBudget save(MealBudget mealBudget);

    /**
     * 일일 예산 ID로 식사 예산 목록 조회
     */
    List<MealBudget> findByDailyBudgetId(Long dailyBudgetId);

    /**
     * 일일 예산 ID와 식사 유형으로 식사 예산 조회
     */
    MealBudget findByDailyBudgetIdAndMealType(Long dailyBudgetId, MealType mealType);

    /**
     * 회원의 특정 날짜 식사 예산 목록 조회
     */
    List<MealBudget> findByMemberIdAndBudgetDate(Long memberId, LocalDate budgetDate);
}

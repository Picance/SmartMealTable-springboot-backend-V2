package com.stdev.smartmealtable.storage.db.budget;

import com.stdev.smartmealtable.domain.budget.MealType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * MealBudgetJpaEntity에 대한 Spring Data JPA Repository
 */
public interface MealBudgetJpaRepository extends JpaRepository<MealBudgetJpaEntity, Long> {

    /**
     * 일일 예산 ID로 식사 예산 목록 조회
     */
    List<MealBudgetJpaEntity> findByDailyBudgetId(Long dailyBudgetId);

    /**
     * 일일 예산 ID와 식사 유형으로 식사 예산 조회
     */
    Optional<MealBudgetJpaEntity> findByDailyBudgetIdAndMealType(Long dailyBudgetId, MealType mealType);

    /**
     * 회원의 특정 날짜 식사 예산 목록 조회
     * (실제로는 daily_budget_id를 통해 조회해야 하지만, 편의를 위해 budget_date로 조회)
     */
    @Query("SELECT m FROM MealBudgetJpaEntity m " +
            "WHERE m.dailyBudgetId IN (SELECT d.budgetId FROM DailyBudgetJpaEntity d WHERE d.memberId = :memberId AND d.budgetDate = :budgetDate)")
    List<MealBudgetJpaEntity> findByMemberIdAndBudgetDate(@Param("memberId") Long memberId, @Param("budgetDate") LocalDate budgetDate);
}

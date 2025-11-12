package com.stdev.smartmealtable.storage.db.budget;

import com.stdev.smartmealtable.domain.expenditure.MealType;
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
    @Query("SELECT m FROM MealBudgetJpaEntity m WHERE m.dailyBudgetId = :dailyBudgetId")
    List<MealBudgetJpaEntity> findByDailyBudgetId(@Param("dailyBudgetId") Long dailyBudgetId);

    /**
     * 일일 예산 ID와 식사 유형으로 식사 예산 조회
     */
    @Query("SELECT m FROM MealBudgetJpaEntity m WHERE m.dailyBudgetId = :dailyBudgetId AND m.mealType = :mealType")
    Optional<MealBudgetJpaEntity> findByDailyBudgetIdAndMealType(
            @Param("dailyBudgetId") Long dailyBudgetId,
            @Param("mealType") MealType mealType
    );

    /**
     * 회원의 특정 날짜 식사 예산 목록 조회
     * (실제로는 daily_budget_id를 통해 조회해야 하지만, 편의를 위해 budget_date로 조회)
     */
    @Query("SELECT m FROM MealBudgetJpaEntity m " +
            "WHERE m.dailyBudgetId IN (SELECT d.budgetId FROM DailyBudgetJpaEntity d WHERE d.memberId = :memberId AND d.budgetDate = :budgetDate)")
    List<MealBudgetJpaEntity> findByMemberIdAndBudgetDate(@Param("memberId") Long memberId, @Param("budgetDate") LocalDate budgetDate);

    /**
     * 날짜 범위로 식사 예산 목록 조회 (포함)
     */
    @Query("SELECT m FROM MealBudgetJpaEntity m " +
            "WHERE m.budgetDate BETWEEN :fromDate AND :toDate")
    List<MealBudgetJpaEntity> findByBudgetDateBetween(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    /**
     * 날짜 범위로 식사 예산 개수 조회 (포함)
     */
    @Query("SELECT COUNT(m) FROM MealBudgetJpaEntity m " +
            "WHERE m.budgetDate BETWEEN :fromDate AND :toDate")
    long countByBudgetDateBetween(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );
}

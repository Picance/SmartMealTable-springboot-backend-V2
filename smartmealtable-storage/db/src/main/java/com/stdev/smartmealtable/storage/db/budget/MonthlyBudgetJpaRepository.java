package com.stdev.smartmealtable.storage.db.budget;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * MonthlyBudgetJpaEntity에 대한 Spring Data JPA Repository
 */
public interface MonthlyBudgetJpaRepository extends JpaRepository<MonthlyBudgetJpaEntity, Long> {

    /**
     * 회원 ID와 예산 월로 월별 예산 조회
     */
    @Query("SELECT m FROM MonthlyBudgetJpaEntity m WHERE m.memberId = :memberId AND m.budgetMonth = :budgetMonth")
    Optional<MonthlyBudgetJpaEntity> findByMemberIdAndBudgetMonth(
            @Param("memberId") Long memberId,
            @Param("budgetMonth") String budgetMonth
    );

    /**
     * 회원 ID로 가장 최근 월별 예산 조회
     */
    @Query("SELECT m FROM MonthlyBudgetJpaEntity m WHERE m.memberId = :memberId ORDER BY m.budgetMonth DESC LIMIT 1")
    Optional<MonthlyBudgetJpaEntity> findLatestByMemberId(@Param("memberId") Long memberId);
}

package com.stdev.smartmealtable.storage.db.budget;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

/**
 * DailyBudgetJpaEntity에 대한 Spring Data JPA Repository
 */
public interface DailyBudgetJpaRepository extends JpaRepository<DailyBudgetJpaEntity, Long> {

    /**
     * 회원 ID와 예산 날짜로 일일 예산 조회
     */
    Optional<DailyBudgetJpaEntity> findByMemberIdAndBudgetDate(Long memberId, LocalDate budgetDate);

    /**
     * 회원 ID로 가장 최근 일일 예산 조회
     */
    @Query("SELECT d FROM DailyBudgetJpaEntity d WHERE d.memberId = :memberId ORDER BY d.budgetDate DESC LIMIT 1")
    Optional<DailyBudgetJpaEntity> findLatestByMemberId(@Param("memberId") Long memberId);
}

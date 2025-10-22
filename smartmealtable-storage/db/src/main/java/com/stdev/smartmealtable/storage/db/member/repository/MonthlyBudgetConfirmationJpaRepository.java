package com.stdev.smartmealtable.storage.db.member.repository;

import com.stdev.smartmealtable.storage.db.member.entity.MonthlyBudgetConfirmationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * 월별 예산 확인 이력 Spring Data JPA Repository
 */
public interface MonthlyBudgetConfirmationJpaRepository extends JpaRepository<MonthlyBudgetConfirmationJpaEntity, Long> {

    /**
     * 회원의 특정 연월 예산 확인 이력 조회
     *
     * @param memberId 회원 ID
     * @param year     연도
     * @param month    월 (1-12)
     * @return 확인 이력 (존재하지 않으면 empty)
     */
    @Query("SELECT m FROM MonthlyBudgetConfirmationJpaEntity m WHERE m.memberId = :memberId AND m.year = :year AND m.month = :month")
    Optional<MonthlyBudgetConfirmationJpaEntity> findByMemberIdAndYearAndMonth(
            @Param("memberId") Long memberId,
            @Param("year") Integer year,
            @Param("month") Integer month
    );

    /**
     * 회원의 특정 연월 예산 확인 여부 확인
     *
     * @param memberId 회원 ID
     * @param year     연도
     * @param month    월 (1-12)
     * @return 확인 이력이 존재하면 true
     */
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN TRUE ELSE FALSE END FROM MonthlyBudgetConfirmationJpaEntity m WHERE m.memberId = :memberId AND m.year = :year AND m.month = :month")
    boolean existsByMemberIdAndYearAndMonth(
            @Param("memberId") Long memberId,
            @Param("year") Integer year,
            @Param("month") Integer month
    );
}

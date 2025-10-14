package com.stdev.smartmealtable.storage.db.member.repository;

import com.stdev.smartmealtable.storage.db.member.entity.MonthlyBudgetConfirmationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

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
    Optional<MonthlyBudgetConfirmationJpaEntity> findByMemberIdAndYearAndMonth(Long memberId, Integer year, Integer month);

    /**
     * 회원의 특정 연월 예산 확인 여부 확인
     *
     * @param memberId 회원 ID
     * @param year     연도
     * @param month    월 (1-12)
     * @return 확인 이력이 존재하면 true
     */
    boolean existsByMemberIdAndYearAndMonth(Long memberId, Integer year, Integer month);
}

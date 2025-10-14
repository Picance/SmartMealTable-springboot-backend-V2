package com.stdev.smartmealtable.domain.member.repository;

import com.stdev.smartmealtable.domain.member.entity.MonthlyBudgetConfirmation;

import java.util.Optional;

/**
 * 월별 예산 확인 이력 Repository 인터페이스
 * Domain 레이어에서 정의하고 Storage 레이어에서 구현
 */
public interface MonthlyBudgetConfirmationRepository {

    /**
     * 월별 예산 확인 이력 저장
     *
     * @param confirmation 저장할 확인 이력
     * @return 저장된 확인 이력 (ID 포함)
     */
    MonthlyBudgetConfirmation save(MonthlyBudgetConfirmation confirmation);

    /**
     * 회원의 특정 연월 예산 확인 이력 조회
     *
     * @param memberId 회원 ID
     * @param year     연도
     * @param month    월 (1-12)
     * @return 확인 이력 (존재하지 않으면 empty)
     */
    Optional<MonthlyBudgetConfirmation> findByMemberIdAndYearAndMonth(Long memberId, Integer year, Integer month);

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

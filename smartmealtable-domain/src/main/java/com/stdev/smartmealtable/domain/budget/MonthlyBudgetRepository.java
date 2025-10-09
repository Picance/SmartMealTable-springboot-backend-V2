package com.stdev.smartmealtable.domain.budget;

import java.util.Optional;

/**
 * 월별 예산 Repository 인터페이스
 */
public interface MonthlyBudgetRepository {
    /**
     * 월별 예산 저장
     */
    MonthlyBudget save(MonthlyBudget monthlyBudget);

    /**
     * 회원 ID와 예산 월로 월별 예산 조회
     */
    Optional<MonthlyBudget> findByMemberIdAndBudgetMonth(Long memberId, String budgetMonth);

    /**
     * 회원 ID로 가장 최근 월별 예산 조회
     */
    Optional<MonthlyBudget> findLatestByMemberId(Long memberId);
}

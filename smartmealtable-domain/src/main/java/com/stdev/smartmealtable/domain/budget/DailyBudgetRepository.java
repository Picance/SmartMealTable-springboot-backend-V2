package com.stdev.smartmealtable.domain.budget;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 일일 예산 Repository 인터페이스
 */
public interface DailyBudgetRepository {
    /**
     * 일일 예산 저장
     */
    DailyBudget save(DailyBudget dailyBudget);

    /**
     * 회원 ID와 예산 날짜로 일일 예산 조회
     */
    Optional<DailyBudget> findByMemberIdAndBudgetDate(Long memberId, LocalDate budgetDate);

    /**
     * 회원 ID로 가장 최근 일일 예산 조회
     */
    Optional<DailyBudget> findLatestByMemberId(Long memberId);

    /**
     * 회원 ID와 특정 날짜(포함) 이후의 모든 일일 예산 조회
     */
    List<DailyBudget> findByMemberIdAndBudgetDateGreaterThanEqual(Long memberId, LocalDate fromDate);
}

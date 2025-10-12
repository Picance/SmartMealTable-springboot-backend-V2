package com.stdev.smartmealtable.domain.expenditure;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 지출 내역 Repository 인터페이스
 */
public interface ExpenditureRepository {
    
    /**
     * 지출 내역 저장
     */
    Expenditure save(Expenditure expenditure);
    
    /**
     * 지출 내역 ID로 조회
     */
    Optional<Expenditure> findById(Long expenditureId);
    
    /**
     * 지출 내역 ID로 조회 (삭제된 것 제외)
     */
    Optional<Expenditure> findByIdAndNotDeleted(Long expenditureId);
    
    /**
     * 회원 ID로 지출 내역 목록 조회 (기간 필터)
     */
    List<Expenditure> findByMemberIdAndDateRange(
            Long memberId,
            LocalDate startDate,
            LocalDate endDate
    );
    
    /**
     * 회원 ID와 지출 ID로 조회 (소유권 검증용)
     */
    Optional<Expenditure> findByIdAndMemberId(Long expenditureId, Long memberId);
    
    /**
     * 지출 내역 삭제 (물리 삭제)
     */
    void delete(Expenditure expenditure);
    
    /**
     * 회원의 특정 월 지출 내역 존재 여부 확인
     */
    boolean existsByMemberIdAndMonth(Long memberId, int year, int month);
    
    /**
     * 기간별 총 지출 금액 조회
     */
    Long getTotalAmountByPeriod(Long memberId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 카테고리별 지출 금액 집계 조회
     */
    Map<Long, Long> getAmountByCategoryForPeriod(Long memberId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 일별 지출 금액 집계 조회
     */
    Map<LocalDate, Long> getDailyAmountForPeriod(Long memberId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 식사 유형별 지출 금액 집계 조회
     */
    Map<MealType, Long> getAmountByMealTypeForPeriod(Long memberId, LocalDate startDate, LocalDate endDate);
}

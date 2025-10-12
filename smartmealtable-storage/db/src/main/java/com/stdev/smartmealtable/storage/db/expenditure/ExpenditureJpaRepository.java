package com.stdev.smartmealtable.storage.db.expenditure;

import com.stdev.smartmealtable.domain.expenditure.MealType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Spring Data JPA Repository for Expenditure
 */
public interface ExpenditureJpaRepository extends JpaRepository<ExpenditureJpaEntity, Long> {
    
    /**
     * ID로 조회 (삭제되지 않은 것만)
     */
    @Query("SELECT e FROM ExpenditureJpaEntity e WHERE e.id = :id AND e.deleted = false")
    Optional<ExpenditureJpaEntity> findByIdAndNotDeleted(@Param("id") Long id);
    
    /**
     * 회원 ID와 기간으로 조회
     */
    @Query("SELECT e FROM ExpenditureJpaEntity e " +
           "WHERE e.memberId = :memberId " +
           "AND e.expendedDate BETWEEN :startDate AND :endDate " +
           "AND e.deleted = false " +
           "ORDER BY e.expendedDate DESC, e.expendedTime DESC")
    List<ExpenditureJpaEntity> findByMemberIdAndDateRange(
            @Param("memberId") Long memberId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    
    /**
     * 회원 ID와 지출 ID로 조회
     */
    @Query("SELECT e FROM ExpenditureJpaEntity e " +
           "WHERE e.id = :expenditureId AND e.memberId = :memberId AND e.deleted = false")
    Optional<ExpenditureJpaEntity> findByIdAndMemberId(
            @Param("expenditureId") Long expenditureId,
            @Param("memberId") Long memberId
    );
    
    /**
     * 회원의 특정 월 지출 내역 존재 여부
     */
    @Query("SELECT COUNT(e) > 0 FROM ExpenditureJpaEntity e " +
           "WHERE e.memberId = :memberId " +
           "AND YEAR(e.expendedDate) = :year " +
           "AND MONTH(e.expendedDate) = :month " +
           "AND e.deleted = false")
    boolean existsByMemberIdAndMonth(
            @Param("memberId") Long memberId,
            @Param("year") int year,
            @Param("month") int month
    );
    
    /**
     * 기간별 총 지출 금액 조회
     */
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM ExpenditureJpaEntity e " +
           "WHERE e.memberId = :memberId " +
           "AND e.expendedDate BETWEEN :startDate AND :endDate " +
           "AND e.deleted = false")
    Long getTotalAmountByPeriod(
            @Param("memberId") Long memberId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    
    /**
     * 카테고리별 지출 금액 집계 조회
     * QueryDSL을 사용하여 Map<Long, Long> 형태로 반환하기 위해 커스텀 쿼리 사용
     */
    @Query("SELECT e.categoryId, SUM(e.amount) FROM ExpenditureJpaEntity e " +
           "WHERE e.memberId = :memberId " +
           "AND e.expendedDate BETWEEN :startDate AND :endDate " +
           "AND e.deleted = false " +
           "AND e.categoryId IS NOT NULL " +
           "GROUP BY e.categoryId")
    List<Object[]> getAmountByCategoryForPeriodRaw(
            @Param("memberId") Long memberId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    
    /**
     * 일별 지출 금액 집계 조회
     */
    @Query("SELECT e.expendedDate, SUM(e.amount) FROM ExpenditureJpaEntity e " +
           "WHERE e.memberId = :memberId " +
           "AND e.expendedDate BETWEEN :startDate AND :endDate " +
           "AND e.deleted = false " +
           "GROUP BY e.expendedDate " +
           "ORDER BY e.expendedDate")
    List<Object[]> getDailyAmountForPeriodRaw(
            @Param("memberId") Long memberId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    
    /**
     * 식사 유형별 지출 금액 집계 조회
     */
    @Query("SELECT e.mealType, SUM(e.amount) FROM ExpenditureJpaEntity e " +
           "WHERE e.memberId = :memberId " +
           "AND e.expendedDate BETWEEN :startDate AND :endDate " +
           "AND e.deleted = false " +
           "AND e.mealType IS NOT NULL " +
           "GROUP BY e.mealType")
    List<Object[]> getAmountByMealTypeForPeriodRaw(
            @Param("memberId") Long memberId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}

package com.stdev.smartmealtable.storage.db.expenditure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
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
}

package com.stdev.smartmealtable.storage.db.member.repository;

import com.stdev.smartmealtable.storage.db.member.entity.AddressHistoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 주소 이력 Spring Data JPA Repository
 */
public interface AddressHistoryJpaRepository extends JpaRepository<AddressHistoryJpaEntity, Long> {
    
    /**
     * 회원의 모든 주소 이력 조회 (주 주소 우선, 등록일 역순)
     */
    @Query("SELECT a FROM AddressHistoryJpaEntity a WHERE a.memberId = :memberId ORDER BY a.isPrimary DESC, a.registeredAt DESC")
    List<AddressHistoryJpaEntity> findAllByMemberId(@Param("memberId") Long memberId);
    
    /**
     * 회원의 주 주소 조회
     */
    @Query("SELECT a FROM AddressHistoryJpaEntity a WHERE a.memberId = :memberId AND a.isPrimary = true")
    Optional<AddressHistoryJpaEntity> findPrimaryByMemberId(@Param("memberId") Long memberId);
    
    /**
     * 회원의 주소 개수 조회
     */
    @Query("SELECT COUNT(a) FROM AddressHistoryJpaEntity a WHERE a.memberId = :memberId")
    long countByMemberId(@Param("memberId") Long memberId);
}

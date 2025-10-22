package com.stdev.smartmealtable.storage.db.preference;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 선호도 정보 Spring Data JPA Repository
 */
public interface PreferenceJpaRepository extends JpaRepository<PreferenceJpaEntity, Long> {

    /**
     * 회원의 모든 선호도 정보 조회
     */
    @Query("SELECT p FROM PreferenceJpaEntity p WHERE p.memberId = :memberId")
    List<PreferenceJpaEntity> findByMemberId(@Param("memberId") Long memberId);

    /**
     * 회원의 특정 카테고리 선호도 조회
     */
    @Query("SELECT p FROM PreferenceJpaEntity p WHERE p.memberId = :memberId AND p.categoryId = :categoryId")
    Optional<PreferenceJpaEntity> findByMemberIdAndCategoryId(
            @Param("memberId") Long memberId,
            @Param("categoryId") Long categoryId
    );

    /**
     * 회원의 모든 선호도 정보 삭제
     */
    @Modifying
    @Query("DELETE FROM PreferenceJpaEntity p WHERE p.memberId = :memberId")
    void deleteByMemberId(@Param("memberId") Long memberId);
}

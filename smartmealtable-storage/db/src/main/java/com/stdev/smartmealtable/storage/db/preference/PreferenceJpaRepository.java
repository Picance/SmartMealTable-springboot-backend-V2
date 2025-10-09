package com.stdev.smartmealtable.storage.db.preference;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 선호도 정보 Spring Data JPA Repository
 */
public interface PreferenceJpaRepository extends JpaRepository<PreferenceJpaEntity, Long> {

    /**
     * 회원의 모든 선호도 정보 조회
     */
    List<PreferenceJpaEntity> findByMemberId(Long memberId);

    /**
     * 회원의 특정 카테고리 선호도 조회
     */
    Optional<PreferenceJpaEntity> findByMemberIdAndCategoryId(Long memberId, Long categoryId);

    /**
     * 회원의 모든 선호도 정보 삭제
     */
    void deleteByMemberId(Long memberId);
}

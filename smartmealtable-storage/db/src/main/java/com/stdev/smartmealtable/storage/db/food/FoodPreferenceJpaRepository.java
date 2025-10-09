package com.stdev.smartmealtable.storage.db.food;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * FoodPreference Spring Data JPA Repository
 */
public interface FoodPreferenceJpaRepository extends JpaRepository<FoodPreferenceJpaEntity, Long> {

    /**
     * 회원의 모든 음식 선호도 조회
     */
    List<FoodPreferenceJpaEntity> findByMemberId(Long memberId);

    /**
     * 회원의 특정 음식 선호도 조회
     */
    Optional<FoodPreferenceJpaEntity> findByMemberIdAndFoodId(Long memberId, Long foodId);

    /**
     * 회원의 선호 음식만 조회 (isPreferred = true)
     */
    List<FoodPreferenceJpaEntity> findByMemberIdAndIsPreferred(Long memberId, Boolean isPreferred);

    /**
     * 회원의 모든 음식 선호도 삭제
     */
    void deleteByMemberId(Long memberId);

    /**
     * 특정 음식 선호도 삭제
     */
    void deleteByMemberIdAndFoodId(Long memberId, Long foodId);

    /**
     * 회원의 선호 음식 개수 조회
     */
    long countByMemberIdAndIsPreferred(Long memberId, Boolean isPreferred);
}

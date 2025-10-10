package com.stdev.smartmealtable.domain.food;

import java.util.List;
import java.util.Optional;

/**
 * 음식 선호도 Repository 인터페이스
 */
public interface FoodPreferenceRepository {

    /**
     * 음식 선호도 저장
     */
    FoodPreference save(FoodPreference foodPreference);

    /**
     * 회원의 모든 음식 선호도 조회
     */
    List<FoodPreference> findByMemberId(Long memberId);

    /**
     * 회원의 특정 음식 선호도 조회
     */
    Optional<FoodPreference> findByMemberIdAndFoodId(Long memberId, Long foodId);

    /**
     * ID로 음식 선호도 조회
     */
    Optional<FoodPreference> findById(Long foodPreferenceId);

    /**
     * 회원의 선호 음식만 조회 (isPreferred = true)
     */
    List<FoodPreference> findByMemberIdAndIsPreferred(Long memberId, Boolean isPreferred);

    /**
     * 회원의 모든 음식 선호도 삭제
     */
    void deleteByMemberId(Long memberId);

    /**
     * 특정 음식 선호도 삭제
     */
    void deleteByMemberIdAndFoodId(Long memberId, Long foodId);

    /**
     * ID로 음식 선호도 삭제
     */
    void deleteById(Long foodPreferenceId);

    /**
     * 회원의 선호 음식 개수 조회
     */
    long countByMemberIdAndIsPreferred(Long memberId, Boolean isPreferred);
}

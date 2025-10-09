package com.stdev.smartmealtable.domain.preference;

import java.util.List;
import java.util.Optional;

/**
 * 선호도 정보 Repository 인터페이스
 */
public interface PreferenceRepository {
    
    /**
     * 선호도 정보 저장
     */
    Preference save(Preference preference);

    /**
     * 회원의 모든 선호도 정보 조회
     */
    List<Preference> findByMemberId(Long memberId);

    /**
     * 회원의 특정 카테고리 선호도 조회
     */
    Optional<Preference> findByMemberIdAndCategoryId(Long memberId, Long categoryId);

    /**
     * 회원의 모든 선호도 정보 삭제
     */
    void deleteByMemberId(Long memberId);
}

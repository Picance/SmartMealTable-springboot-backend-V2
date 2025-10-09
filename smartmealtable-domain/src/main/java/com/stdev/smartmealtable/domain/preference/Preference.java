package com.stdev.smartmealtable.domain.preference;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원의 카테고리별 선호도 정보를 나타내는 도메인 엔티티
 * 
 * 추천 알고리즘의 안정성 점수 계산에 사용됨
 * weight 값: 100 (좋아요), 0 (보통), -100 (싫어요)
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Preference {
    
    private Long preferenceId;
    private Long memberId;
    private Long categoryId;
    private Integer weight;

    /**
     * 새로운 선호도 정보 생성 (팩토리 메서드)
     */
    public static Preference create(Long memberId, Long categoryId, Integer weight) {
        validateWeight(weight);
        
        Preference preference = new Preference();
        preference.memberId = memberId;
        preference.categoryId = categoryId;
        preference.weight = weight;
        return preference;
    }

    /**
     * JPA Entity에서 Domain Entity로 변환 시 ID 보존 (reconstitute 패턴)
     */
    public static Preference reconstitute(Long preferenceId, Long memberId, Long categoryId, Integer weight) {
        validateWeight(weight);
        
        Preference preference = new Preference();
        preference.preferenceId = preferenceId;
        preference.memberId = memberId;
        preference.categoryId = categoryId;
        preference.weight = weight;
        return preference;
    }

    /**
     * 선호도 가중치 변경
     */
    public void changeWeight(Integer newWeight) {
        validateWeight(newWeight);
        this.weight = newWeight;
    }

    /**
     * weight 값 검증: -100, 0, 100만 허용
     */
    private static void validateWeight(Integer weight) {
        if (weight == null) {
            throw new IllegalArgumentException("선호도 가중치는 필수입니다.");
        }
        if (weight != -100 && weight != 0 && weight != 100) {
            throw new IllegalArgumentException("선호도 가중치는 -100, 0, 100 중 하나여야 합니다. 입력값: " + weight);
        }
    }
}

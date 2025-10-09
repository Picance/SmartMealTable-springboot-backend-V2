package com.stdev.smartmealtable.domain.food;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 개별 음식 선호도 도메인 엔티티
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FoodPreference {

    private Long foodPreferenceId;
    private Long memberId;
    private Long foodId;
    private Boolean isPreferred; // true: 선호, false: 비선호
    private LocalDateTime preferredAt;

    /**
     * 음식 선호도 생성 (팩토리 메서드)
     */
    public static FoodPreference create(Long memberId, Long foodId) {
        FoodPreference preference = new FoodPreference();
        preference.memberId = memberId;
        preference.foodId = foodId;
        preference.isPreferred = true; // 기본값: 선호
        preference.preferredAt = LocalDateTime.now();
        return preference;
    }

    /**
     * JPA Entity에서 Domain Entity로 변환 시 사용 (reconstitute 패턴)
     */
    public static FoodPreference reconstitute(
            Long foodPreferenceId,
            Long memberId,
            Long foodId,
            Boolean isPreferred,
            LocalDateTime preferredAt
    ) {
        FoodPreference preference = new FoodPreference();
        preference.foodPreferenceId = foodPreferenceId;
        preference.memberId = memberId;
        preference.foodId = foodId;
        preference.isPreferred = isPreferred;
        preference.preferredAt = preferredAt;
        return preference;
    }

    /**
     * 선호도 변경
     */
    public void changePreference(Boolean isPreferred) {
        this.isPreferred = isPreferred;
        this.preferredAt = LocalDateTime.now();
    }
}

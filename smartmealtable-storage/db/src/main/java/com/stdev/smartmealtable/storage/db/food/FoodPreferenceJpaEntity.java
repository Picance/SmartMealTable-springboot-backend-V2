package com.stdev.smartmealtable.storage.db.food;

import com.stdev.smartmealtable.domain.food.FoodPreference;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 개별 음식 선호도 JPA Entity
 */
@Entity
@Table(name = "food_preference")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FoodPreferenceJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "food_preference_id")
    private Long foodPreferenceId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "food_id", nullable = false)
    private Long foodId;

    @Column(name = "is_preferred", nullable = false)
    private Boolean isPreferred;

    @Column(name = "preferred_at", nullable = false)
    private LocalDateTime preferredAt;

    // created_at, updated_at은 DB DEFAULT CURRENT_TIMESTAMP로 관리 (도메인에 노출 안 함)
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    /**
     * JPA Entity → Domain 변환
     */
    public FoodPreference toDomain() {
        return FoodPreference.reconstitute(
                this.foodPreferenceId,
                this.memberId,
                this.foodId,
                this.isPreferred,
                this.preferredAt
        );
    }

    /**
     * Domain → JPA Entity 변환 (for save)
     */
    public static FoodPreferenceJpaEntity fromDomain(FoodPreference preference) {
        FoodPreferenceJpaEntity entity = new FoodPreferenceJpaEntity();
        entity.foodPreferenceId = preference.getFoodPreferenceId();
        entity.memberId = preference.getMemberId();
        entity.foodId = preference.getFoodId();
        entity.isPreferred = preference.getIsPreferred();
        entity.preferredAt = preference.getPreferredAt();
        return entity;
    }
}

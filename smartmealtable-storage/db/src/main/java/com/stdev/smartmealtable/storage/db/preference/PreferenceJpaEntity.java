package com.stdev.smartmealtable.storage.db.preference;

import com.stdev.smartmealtable.domain.preference.Preference;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 선호도 정보 JPA Entity
 */
@Entity
@Table(name = "preference")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PreferenceJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "preference_id")
    private Long preferenceId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "weight", nullable = false)
    private Integer weight;

    // created_at, updated_at은 DB DEFAULT CURRENT_TIMESTAMP로 관리 (도메인에 노출 안 함)
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    /**
     * Domain Entity → JPA Entity 변환 (신규 생성)
     */
    public static PreferenceJpaEntity fromDomain(Preference preference) {
        PreferenceJpaEntity entity = new PreferenceJpaEntity();
        entity.memberId = preference.getMemberId();
        entity.categoryId = preference.getCategoryId();
        entity.weight = preference.getWeight();
        return entity;
    }

    /**
     * Domain Entity → JPA Entity 변환 (ID 보존)
     */
    public static PreferenceJpaEntity fromDomainWithId(Preference preference) {
        PreferenceJpaEntity entity = new PreferenceJpaEntity();
        entity.preferenceId = preference.getPreferenceId();
        entity.memberId = preference.getMemberId();
        entity.categoryId = preference.getCategoryId();
        entity.weight = preference.getWeight();
        return entity;
    }

    /**
     * JPA Entity → Domain Entity 변환
     */
    public Preference toDomain() {
        return Preference.reconstitute(
            this.preferenceId,
            this.memberId,
            this.categoryId,
            this.weight
        );
    }

    /**
     * weight 값 변경
     */
    public void changeWeight(Integer newWeight) {
        this.weight = newWeight;
    }
}

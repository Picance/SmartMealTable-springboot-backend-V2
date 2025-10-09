package com.stdev.smartmealtable.storage.db.policy;

import com.stdev.smartmealtable.domain.policy.entity.Policy;
import com.stdev.smartmealtable.domain.policy.entity.PolicyType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Policy JPA 엔티티
 */
@Entity
@Table(name = "policy")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PolicyJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_id")
    private Long policyId;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private PolicyType type;

    @Column(name = "version", nullable = false, length = 20)
    private String version;

    @Column(name = "is_mandatory", nullable = false)
    private Boolean isMandatory;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    // created_at, updated_at은 DB DEFAULT CURRENT_TIMESTAMP로 관리 (도메인에 노출 안 함)
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    /**
     * Domain → JPA Entity 변환
     */
    public static PolicyJpaEntity from(Policy policy) {
        PolicyJpaEntity entity = new PolicyJpaEntity();
        entity.policyId = policy.getPolicyId();
        entity.title = policy.getTitle();
        entity.content = policy.getContent();
        entity.type = policy.getType();
        entity.version = policy.getVersion();
        entity.isMandatory = policy.getIsMandatory();
        entity.isActive = policy.getIsActive();
        return entity;
    }

    /**
     * JPA Entity → Domain 변환
     */
    public Policy toDomain() {
        return Policy.reconstitute(
                this.policyId,
                this.title,
                this.content,
                this.type,
                this.version,
                this.isMandatory,
                this.isActive
        );
    }
}

package com.stdev.smartmealtable.storage.db.policy;

import com.stdev.smartmealtable.domain.policy.entity.PolicyAgreement;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * PolicyAgreement JPA 엔티티
 */
@Entity
@Table(
        name = "policy_agreement",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_policy_member_auth",
                columnNames = {"policy_id", "member_authentication_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PolicyAgreementJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_agreement_id")
    private Long policyAgreementId;

    @Column(name = "policy_id", nullable = false)
    private Long policyId;

    @Column(name = "member_authentication_id", nullable = false)
    private Long memberAuthenticationId;

    @Column(name = "is_agreed", nullable = false)
    private Boolean isAgreed;

    @Column(name = "agreed_at", nullable = false)
    private LocalDateTime agreedAt;

    // created_at, updated_at은 DB DEFAULT CURRENT_TIMESTAMP로 관리 (도메인에 노출 안 함)
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    /**
     * Domain → JPA Entity 변환
     */
    public static PolicyAgreementJpaEntity from(PolicyAgreement policyAgreement) {
        PolicyAgreementJpaEntity entity = new PolicyAgreementJpaEntity();
        entity.policyAgreementId = policyAgreement.getPolicyAgreementId();
        entity.policyId = policyAgreement.getPolicyId();
        entity.memberAuthenticationId = policyAgreement.getMemberAuthenticationId();
        entity.isAgreed = policyAgreement.getIsAgreed();
        entity.agreedAt = policyAgreement.getAgreedAt();
        return entity;
    }

    /**
     * JPA Entity → Domain 변환
     */
    public PolicyAgreement toDomain() {
        return PolicyAgreement.reconstitute(
                this.policyAgreementId,
                this.policyId,
                this.memberAuthenticationId,
                this.isAgreed,
                this.agreedAt
        );
    }
}

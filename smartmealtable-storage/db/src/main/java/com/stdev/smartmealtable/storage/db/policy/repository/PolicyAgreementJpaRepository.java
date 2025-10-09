package com.stdev.smartmealtable.storage.db.policy.repository;

import com.stdev.smartmealtable.storage.db.policy.PolicyAgreementJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * PolicyAgreement Spring Data JPA Repository
 */
public interface PolicyAgreementJpaRepository extends JpaRepository<PolicyAgreementJpaEntity, Long> {

    /**
     * 회원의 모든 약관 동의 내역 조회
     */
    List<PolicyAgreementJpaEntity> findByMemberAuthenticationId(Long memberAuthenticationId);

    /**
     * 특정 회원의 특정 약관 동의 여부 확인
     */
    boolean existsByMemberAuthenticationIdAndPolicyId(Long memberAuthenticationId, Long policyId);
}

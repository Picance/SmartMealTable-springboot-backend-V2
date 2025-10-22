package com.stdev.smartmealtable.storage.db.policy.repository;

import com.stdev.smartmealtable.storage.db.policy.PolicyAgreementJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * PolicyAgreement Spring Data JPA Repository
 */
public interface PolicyAgreementJpaRepository extends JpaRepository<PolicyAgreementJpaEntity, Long> {

    /**
     * 회원의 모든 약관 동의 내역 조회
     */
    @Query("SELECT p FROM PolicyAgreementJpaEntity p WHERE p.memberAuthenticationId = :memberAuthenticationId")
    List<PolicyAgreementJpaEntity> findByMemberAuthenticationId(@Param("memberAuthenticationId") Long memberAuthenticationId);

    /**
     * 특정 회원의 특정 약관 동의 여부 확인
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN TRUE ELSE FALSE END FROM PolicyAgreementJpaEntity p WHERE p.memberAuthenticationId = :memberAuthenticationId AND p.policyId = :policyId")
    boolean existsByMemberAuthenticationIdAndPolicyId(
            @Param("memberAuthenticationId") Long memberAuthenticationId,
            @Param("policyId") Long policyId
    );
}

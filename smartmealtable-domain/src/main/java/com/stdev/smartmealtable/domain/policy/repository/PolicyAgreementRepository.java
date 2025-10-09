package com.stdev.smartmealtable.domain.policy.repository;

import com.stdev.smartmealtable.domain.policy.entity.PolicyAgreement;

import java.util.List;

/**
 * 약관 동의 Repository 인터페이스
 */
public interface PolicyAgreementRepository {

    /**
     * 약관 동의 내역 저장
     */
    PolicyAgreement save(PolicyAgreement policyAgreement);

    /**
     * 회원의 모든 약관 동의 내역 조회
     */
    List<PolicyAgreement> findByMemberAuthenticationId(Long memberAuthenticationId);

    /**
     * 특정 회원의 특정 약관 동의 여부 확인
     */
    boolean existsByMemberAuthenticationIdAndPolicyId(Long memberAuthenticationId, Long policyId);
}

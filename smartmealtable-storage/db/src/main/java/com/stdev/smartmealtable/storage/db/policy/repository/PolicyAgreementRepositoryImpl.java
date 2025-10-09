package com.stdev.smartmealtable.storage.db.policy.repository;

import com.stdev.smartmealtable.domain.policy.entity.PolicyAgreement;
import com.stdev.smartmealtable.domain.policy.repository.PolicyAgreementRepository;
import com.stdev.smartmealtable.storage.db.policy.PolicyAgreementJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * PolicyAgreement Repository 구현체
 */
@Repository
@RequiredArgsConstructor
public class PolicyAgreementRepositoryImpl implements PolicyAgreementRepository {

    private final PolicyAgreementJpaRepository policyAgreementJpaRepository;

    @Override
    public PolicyAgreement save(PolicyAgreement policyAgreement) {
        PolicyAgreementJpaEntity entity = PolicyAgreementJpaEntity.from(policyAgreement);
        PolicyAgreementJpaEntity saved = policyAgreementJpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public List<PolicyAgreement> findByMemberAuthenticationId(Long memberAuthenticationId) {
        return policyAgreementJpaRepository.findByMemberAuthenticationId(memberAuthenticationId)
                .stream()
                .map(PolicyAgreementJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByMemberAuthenticationIdAndPolicyId(Long memberAuthenticationId, Long policyId) {
        return policyAgreementJpaRepository.existsByMemberAuthenticationIdAndPolicyId(
                memberAuthenticationId,
                policyId
        );
    }
}

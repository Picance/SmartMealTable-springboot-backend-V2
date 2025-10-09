package com.stdev.smartmealtable.storage.db.policy.repository;

import com.stdev.smartmealtable.domain.policy.entity.Policy;
import com.stdev.smartmealtable.domain.policy.repository.PolicyRepository;
import com.stdev.smartmealtable.storage.db.policy.PolicyJpaEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Policy Repository 구현체
 */
@Repository
public class PolicyRepositoryImpl implements PolicyRepository {

    private final PolicyJpaRepository jpaRepository;

    public PolicyRepositoryImpl(PolicyJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Policy save(Policy policy) {
        PolicyJpaEntity jpaEntity = PolicyJpaEntity.from(policy);
        PolicyJpaEntity saved = jpaRepository.save(jpaEntity);
        return saved.toDomain();
    }

    @Override
    public Optional<Policy> findById(Long policyId) {
        return jpaRepository.findById(policyId)
                .map(PolicyJpaEntity::toDomain);
    }

    @Override
    public List<Policy> findAllActive() {
        return jpaRepository.findAllActive().stream()
                .map(PolicyJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Policy> findAll() {
        return jpaRepository.findAll().stream()
                .map(PolicyJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
}

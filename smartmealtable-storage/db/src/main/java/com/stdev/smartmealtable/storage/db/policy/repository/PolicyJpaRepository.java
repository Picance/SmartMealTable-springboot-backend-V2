package com.stdev.smartmealtable.storage.db.policy.repository;

import com.stdev.smartmealtable.storage.db.policy.PolicyJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Policy Spring Data JPA Repository
 */
public interface PolicyJpaRepository extends JpaRepository<PolicyJpaEntity, Long> {

    /**
     * 활성화된 약관만 조회
     */
    @Query("SELECT p FROM PolicyJpaEntity p WHERE p.isActive = true ORDER BY p.type, p.policyId")
    List<PolicyJpaEntity> findAllActive();
}

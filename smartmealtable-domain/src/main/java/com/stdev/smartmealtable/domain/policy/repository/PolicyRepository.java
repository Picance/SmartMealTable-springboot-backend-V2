package com.stdev.smartmealtable.domain.policy.repository;

import com.stdev.smartmealtable.domain.policy.entity.Policy;

import java.util.List;
import java.util.Optional;

/**
 * Policy Repository 인터페이스
 * Storage 계층에서 구현
 */
public interface PolicyRepository {

    /**
     * 약관 저장
     */
    Policy save(Policy policy);

    /**
     * 약관 조회 by ID
     */
    Optional<Policy> findById(Long policyId);

    /**
     * 활성화된 모든 약관 조회
     */
    List<Policy> findAllActive();

    /**
     * 모든 약관 조회 (테스트용)
     */
    List<Policy> findAll();
}

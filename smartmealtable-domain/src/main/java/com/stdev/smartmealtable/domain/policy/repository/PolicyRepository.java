package com.stdev.smartmealtable.domain.policy.repository;

import com.stdev.smartmealtable.domain.policy.entity.Policy;
import com.stdev.smartmealtable.domain.policy.entity.PolicyPageResult;

import java.util.List;
import java.util.Optional;

/**
 * Policy Repository 인터페이스 (순수 Java)
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
     * 모든 약관 조회
     */
    List<Policy> findAll();

    /**
     * 약관 제목으로 검색 (페이징)
     */
    PolicyPageResult searchByTitle(String title, Boolean isActive, int page, int size);

    /**
     * 약관 제목 중복 확인
     */
    boolean existsByTitle(String title);

    /**
     * 특정 ID 제외 제목 중복 확인
     */
    boolean existsByTitleAndIdNot(String title, Long policyId);

    /**
     * 약관 삭제
     */
    void deleteById(Long policyId);

    /**
     * 약관에 동의한 사용자가 있는지 확인
     */
    boolean hasAgreements(Long policyId);
}

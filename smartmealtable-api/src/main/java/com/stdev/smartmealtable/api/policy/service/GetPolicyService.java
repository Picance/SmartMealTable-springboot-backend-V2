package com.stdev.smartmealtable.api.policy.service;

import com.stdev.smartmealtable.api.policy.service.dto.GetPolicyServiceResponse;
import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.domain.policy.entity.Policy;
import com.stdev.smartmealtable.domain.policy.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 약관 상세 조회 Application Service
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GetPolicyService {

    private final PolicyRepository policyRepository;

    /**
     * 약관 상세 조회
     *
     * @param policyId 약관 ID
     * @return 약관 상세 정보
     */
    public GetPolicyServiceResponse getPolicy(Long policyId) {
        log.info("약관 상세 조회 서비스 호출 - policyId: {}", policyId);

        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new BusinessException(ErrorType.POLICY_NOT_FOUND));

        return GetPolicyServiceResponse.from(policy);
    }
}

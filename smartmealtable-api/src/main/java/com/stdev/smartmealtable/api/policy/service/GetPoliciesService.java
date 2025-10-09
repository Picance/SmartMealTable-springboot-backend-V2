package com.stdev.smartmealtable.api.policy.service;

import com.stdev.smartmealtable.api.policy.service.dto.GetPoliciesServiceResponse;
import com.stdev.smartmealtable.domain.policy.entity.Policy;
import com.stdev.smartmealtable.domain.policy.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 약관 목록 조회 Application Service
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GetPoliciesService {

    private final PolicyRepository policyRepository;

    /**
     * 활성화된 약관 목록 조회
     *
     * @return 약관 목록
     */
    public GetPoliciesServiceResponse getPolicies() {
        log.info("약관 목록 조회 서비스 호출");

        List<Policy> policies = policyRepository.findAllActive();

        List<GetPoliciesServiceResponse.PolicyInfo> policyInfos = policies.stream()
                .map(GetPoliciesServiceResponse.PolicyInfo::from)
                .collect(Collectors.toList());

        return new GetPoliciesServiceResponse(policyInfos);
    }
}

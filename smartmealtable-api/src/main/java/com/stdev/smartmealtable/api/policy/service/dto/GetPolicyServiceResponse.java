package com.stdev.smartmealtable.api.policy.service.dto;

import com.stdev.smartmealtable.domain.policy.entity.Policy;
import com.stdev.smartmealtable.domain.policy.entity.PolicyType;

/**
 * 약관 상세 조회 서비스 응답 DTO
 */
public record GetPolicyServiceResponse(
        Long policyId,
        String title,
        String content,
        PolicyType type,
        String version
) {
    public static GetPolicyServiceResponse from(Policy policy) {
        return new GetPolicyServiceResponse(
                policy.getPolicyId(),
                policy.getTitle(),
                policy.getContent(),
                policy.getType(),
                policy.getVersion()
        );
    }
}

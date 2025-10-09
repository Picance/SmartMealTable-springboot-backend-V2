package com.stdev.smartmealtable.api.policy.service.dto;

import com.stdev.smartmealtable.domain.policy.entity.Policy;
import com.stdev.smartmealtable.domain.policy.entity.PolicyType;

import java.util.List;

/**
 * 약관 목록 조회 서비스 응답 DTO
 */
public record GetPoliciesServiceResponse(
        List<PolicyInfo> policies
) {
    public record PolicyInfo(
            Long policyId,
            String title,
            PolicyType type,
            String version
    ) {
        public static PolicyInfo from(Policy policy) {
            return new PolicyInfo(
                    policy.getPolicyId(),
                    policy.getTitle(),
                    policy.getType(),
                    policy.getVersion()
            );
        }
    }
}

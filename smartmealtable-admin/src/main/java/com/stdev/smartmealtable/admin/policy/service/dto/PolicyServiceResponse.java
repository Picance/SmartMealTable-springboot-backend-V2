package com.stdev.smartmealtable.admin.policy.service.dto;

import com.stdev.smartmealtable.domain.policy.entity.Policy;
import com.stdev.smartmealtable.domain.policy.entity.PolicyType;

/**
 * 약관 상세 조회 Service Response DTO
 */
public record PolicyServiceResponse(
        Long policyId,
        String title,
        String content,
        PolicyType type,
        String version,
        Boolean isMandatory,
        Boolean isActive
) {
    public static PolicyServiceResponse from(Policy policy) {
        return new PolicyServiceResponse(
                policy.getPolicyId(),
                policy.getTitle(),
                policy.getContent(),
                policy.getType(),
                policy.getVersion(),
                policy.getIsMandatory(),
                policy.getIsActive()
        );
    }
}

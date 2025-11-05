package com.stdev.smartmealtable.admin.policy.controller.dto;

import com.stdev.smartmealtable.admin.policy.service.dto.PolicyServiceResponse;
import com.stdev.smartmealtable.domain.policy.entity.PolicyType;

/**
 * 약관 상세 조회 Response DTO
 */
public record PolicyResponse(
        Long policyId,
        String title,
        String content,
        PolicyType type,
        String version,
        Boolean isMandatory,
        Boolean isActive
) {
    public static PolicyResponse from(PolicyServiceResponse serviceResponse) {
        return new PolicyResponse(
                serviceResponse.policyId(),
                serviceResponse.title(),
                serviceResponse.content(),
                serviceResponse.type(),
                serviceResponse.version(),
                serviceResponse.isMandatory(),
                serviceResponse.isActive()
        );
    }
}

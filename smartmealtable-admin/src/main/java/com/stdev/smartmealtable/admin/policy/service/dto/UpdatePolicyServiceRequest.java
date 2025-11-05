package com.stdev.smartmealtable.admin.policy.service.dto;

import com.stdev.smartmealtable.domain.policy.entity.PolicyType;

/**
 * 약관 수정 Service Request DTO
 */
public record UpdatePolicyServiceRequest(
        String title,
        String content,
        PolicyType type,
        String version,
        Boolean isMandatory
) {
}

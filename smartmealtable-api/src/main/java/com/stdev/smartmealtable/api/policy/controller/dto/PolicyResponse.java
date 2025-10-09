package com.stdev.smartmealtable.api.policy.controller.dto;

import com.stdev.smartmealtable.domain.policy.entity.PolicyType;

/**
 * 약관 응답 DTO
 */
public record PolicyResponse(
        Long policyId,
        String title,
        String content,
        PolicyType type,
        String version
) {
}

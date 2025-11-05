package com.stdev.smartmealtable.admin.policy.service.dto;

import java.util.List;

/**
 * 약관 목록 조회 Service Response DTO
 */
public record PolicyListServiceResponse(
        List<PolicyServiceResponse> policies,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static PolicyListServiceResponse of(
            List<PolicyServiceResponse> policies,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {
        return new PolicyListServiceResponse(
                policies,
                page,
                size,
                totalElements,
                totalPages
        );
    }
}

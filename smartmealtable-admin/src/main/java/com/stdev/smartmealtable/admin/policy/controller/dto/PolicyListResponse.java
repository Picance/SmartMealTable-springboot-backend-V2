package com.stdev.smartmealtable.admin.policy.controller.dto;

import com.stdev.smartmealtable.admin.policy.service.dto.PolicyListServiceResponse;
import com.stdev.smartmealtable.admin.policy.service.dto.PolicyServiceResponse;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 약관 목록 조회 Response DTO
 */
public record PolicyListResponse(
        List<PolicySummary> policies,
        PageInfo pageInfo
) {
    public static PolicyListResponse from(PolicyListServiceResponse serviceResponse) {
        List<PolicySummary> summaries = serviceResponse.policies().stream()
                .map(PolicySummary::from)
                .collect(Collectors.toList());
        
        PageInfo pageInfo = new PageInfo(
                serviceResponse.page(),
                serviceResponse.size(),
                serviceResponse.totalElements(),
                serviceResponse.totalPages()
        );
        
        return new PolicyListResponse(summaries, pageInfo);
    }

    /**
     * 약관 요약 정보
     */
    public record PolicySummary(
            Long policyId,
            String title,
            String type,
            String version,
            Boolean isMandatory,
            Boolean isActive
    ) {
        public static PolicySummary from(PolicyServiceResponse serviceResponse) {
            return new PolicySummary(
                    serviceResponse.policyId(),
                    serviceResponse.title(),
                    serviceResponse.type().name(),
                    serviceResponse.version(),
                    serviceResponse.isMandatory(),
                    serviceResponse.isActive()
            );
        }
    }

    /**
     * 페이지 정보
     */
    public record PageInfo(
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {
    }
}

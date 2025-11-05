package com.stdev.smartmealtable.admin.policy.service.dto;

/**
 * 약관 목록 조회 Service Request DTO
 */
public record PolicyListServiceRequest(
        String title,
        Boolean isActive,
        int page,
        int size
) {
}

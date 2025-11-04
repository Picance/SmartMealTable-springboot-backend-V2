package com.stdev.smartmealtable.admin.category.service.dto;

/**
 * 카테고리 목록 조회 요청 DTO
 */
public record CategoryListServiceRequest(
        String name,
        int page,
        int size
) {
    public static CategoryListServiceRequest of(String name, int page, int size) {
        return new CategoryListServiceRequest(name, page, size);
    }
}

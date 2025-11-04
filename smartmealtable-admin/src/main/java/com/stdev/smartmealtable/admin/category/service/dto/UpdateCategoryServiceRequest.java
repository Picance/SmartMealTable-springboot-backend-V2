package com.stdev.smartmealtable.admin.category.service.dto;

/**
 * 카테고리 수정 요청 DTO
 */
public record UpdateCategoryServiceRequest(
        String name
) {
    public static UpdateCategoryServiceRequest of(String name) {
        return new UpdateCategoryServiceRequest(name);
    }
}

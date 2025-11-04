package com.stdev.smartmealtable.admin.category.service.dto;

/**
 * 카테고리 생성 요청 DTO
 */
public record CreateCategoryServiceRequest(
        String name
) {
    public static CreateCategoryServiceRequest of(String name) {
        return new CreateCategoryServiceRequest(name);
    }
}

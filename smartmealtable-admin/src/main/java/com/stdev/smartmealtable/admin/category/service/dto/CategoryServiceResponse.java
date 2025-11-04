package com.stdev.smartmealtable.admin.category.service.dto;

import com.stdev.smartmealtable.domain.category.Category;

/**
 * 카테고리 응답 DTO
 */
public record CategoryServiceResponse(
        Long categoryId,
        String name
) {
    public static CategoryServiceResponse from(Category category) {
        return new CategoryServiceResponse(
                category.getCategoryId(),
                category.getName()
        );
    }
}

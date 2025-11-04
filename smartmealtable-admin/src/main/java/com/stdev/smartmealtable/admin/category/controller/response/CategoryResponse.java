package com.stdev.smartmealtable.admin.category.controller.response;

import com.stdev.smartmealtable.admin.category.service.dto.CategoryServiceResponse;

/**
 * 카테고리 응답 DTO
 */
public record CategoryResponse(
        Long categoryId,
        String name
) {
    public static CategoryResponse from(CategoryServiceResponse serviceResponse) {
        return new CategoryResponse(
                serviceResponse.categoryId(),
                serviceResponse.name()
        );
    }
}

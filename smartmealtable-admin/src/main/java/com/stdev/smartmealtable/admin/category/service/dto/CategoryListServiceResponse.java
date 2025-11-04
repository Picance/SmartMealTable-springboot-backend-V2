package com.stdev.smartmealtable.admin.category.service.dto;

import java.util.List;

/**
 * 카테고리 목록 응답 DTO (페이징 포함)
 */
public record CategoryListServiceResponse(
        List<CategoryServiceResponse> categories,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static CategoryListServiceResponse of(
            List<CategoryServiceResponse> categories,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {
        return new CategoryListServiceResponse(categories, page, size, totalElements, totalPages);
    }
}

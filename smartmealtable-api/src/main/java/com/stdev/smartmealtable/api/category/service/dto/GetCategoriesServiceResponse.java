package com.stdev.smartmealtable.api.category.service.dto;

import com.stdev.smartmealtable.domain.category.Category;

import java.util.List;

/**
 * 카테고리 목록 조회 서비스 응답 DTO
 */
public record GetCategoriesServiceResponse(
        List<CategoryInfo> categories
) {
    public record CategoryInfo(
            Long categoryId,
            String name
    ) {
        public static CategoryInfo from(Category category) {
            return new CategoryInfo(
                    category.getCategoryId(),
                    category.getName()
            );
        }
    }
}

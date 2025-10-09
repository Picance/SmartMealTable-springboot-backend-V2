package com.stdev.smartmealtable.api.category.controller.dto;

/**
 * 카테고리 조회 응답 DTO
 */
public record CategoryResponse(
        Long categoryId,
        String name
) {
}

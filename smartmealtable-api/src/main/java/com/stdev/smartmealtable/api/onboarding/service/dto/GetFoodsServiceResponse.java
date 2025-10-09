package com.stdev.smartmealtable.api.onboarding.service.dto;

import java.util.List;

/**
 * 음식 목록 조회 Service Response
 */
public record GetFoodsServiceResponse(
        List<FoodInfo> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean last,
        boolean first
) {
    /**
     * 음식 정보
     */
    public record FoodInfo(
            Long foodId,
            String foodName,
            Long categoryId,
            String categoryName,
            String imageUrl,
            String description,
            Integer averagePrice
    ) {
    }
}

package com.stdev.smartmealtable.admin.food.service.dto;

import com.stdev.smartmealtable.domain.food.FoodPageResult;

import java.util.List;

/**
 * 음식 목록 응답 DTO
 */
public record FoodListServiceResponse(
        List<FoodServiceResponse> foods,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public static FoodListServiceResponse from(FoodPageResult pageResult) {
        List<FoodServiceResponse> foods = pageResult.content().stream()
                .map(FoodServiceResponse::from)
                .toList();
        
        return new FoodListServiceResponse(
                foods,
                pageResult.page(),
                pageResult.size(),
                pageResult.totalElements(),
                pageResult.totalPages(),
                pageResult.hasNext()
        );
    }
}

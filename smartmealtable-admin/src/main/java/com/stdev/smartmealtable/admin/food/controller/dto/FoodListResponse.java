package com.stdev.smartmealtable.admin.food.controller.dto;

import com.stdev.smartmealtable.admin.food.service.dto.FoodListServiceResponse;

import java.util.List;

/**
 * 음식 목록 응답 DTO
 */
public record FoodListResponse(
        List<FoodResponse> foods,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public static FoodListResponse from(FoodListServiceResponse serviceResponse) {
        List<FoodResponse> foods = serviceResponse.foods().stream()
                .map(FoodResponse::from)
                .toList();
        
        return new FoodListResponse(
                foods,
                serviceResponse.page(),
                serviceResponse.size(),
                serviceResponse.totalElements(),
                serviceResponse.totalPages(),
                serviceResponse.hasNext()
        );
    }
}

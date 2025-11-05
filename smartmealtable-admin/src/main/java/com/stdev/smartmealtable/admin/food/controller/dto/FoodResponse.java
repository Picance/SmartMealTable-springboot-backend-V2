package com.stdev.smartmealtable.admin.food.controller.dto;

import com.stdev.smartmealtable.admin.food.service.dto.FoodServiceResponse;

/**
 * 음식 응답 DTO
 */
public record FoodResponse(
        Long foodId,
        String foodName,
        Long storeId,
        Long categoryId,
        String description,
        String imageUrl,
        Integer averagePrice
) {
    public static FoodResponse from(FoodServiceResponse serviceResponse) {
        return new FoodResponse(
                serviceResponse.foodId(),
                serviceResponse.foodName(),
                serviceResponse.storeId(),
                serviceResponse.categoryId(),
                serviceResponse.description(),
                serviceResponse.imageUrl(),
                serviceResponse.averagePrice()
        );
    }
}

package com.stdev.smartmealtable.admin.food.service.dto;

import com.stdev.smartmealtable.domain.food.Food;

/**
 * 음식 응답 DTO
 */
public record FoodServiceResponse(
        Long foodId,
        String foodName,
        Long storeId,
        Long categoryId,
        String description,
        String imageUrl,
        Integer averagePrice
) {
    public static FoodServiceResponse from(Food food) {
        return new FoodServiceResponse(
                food.getFoodId(),
                food.getFoodName(),
                food.getStoreId(),
                food.getCategoryId(),
                food.getDescription(),
                food.getImageUrl(),
                food.getAveragePrice()
        );
    }
}

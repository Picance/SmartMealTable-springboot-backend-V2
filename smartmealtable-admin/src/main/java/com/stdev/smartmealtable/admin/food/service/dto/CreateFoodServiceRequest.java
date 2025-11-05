package com.stdev.smartmealtable.admin.food.service.dto;

/**
 * 음식 생성 요청 DTO
 */
public record CreateFoodServiceRequest(
        String foodName,
        Long storeId,
        Long categoryId,
        String description,
        String imageUrl,
        Integer averagePrice
) {
}

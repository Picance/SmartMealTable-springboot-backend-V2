package com.stdev.smartmealtable.admin.food.service.dto;

/**
 * 음식 수정 요청 DTO
 */
public record UpdateFoodServiceRequest(
        String foodName,
        Long categoryId,
        String description,
        String imageUrl,
        Integer averagePrice
) {
}

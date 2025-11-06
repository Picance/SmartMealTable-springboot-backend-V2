package com.stdev.smartmealtable.admin.food.service.dto;

/**
 * 음식 수정 요청 DTO - v2.0
 * 
 * <p>isMain, displayOrder 필드를 포함합니다.</p>
 */
public record UpdateFoodServiceRequest(
        String foodName,
        Long categoryId,
        String description,
        String imageUrl,
        Integer averagePrice,
        Boolean isMain,
        Integer displayOrder
) {
}

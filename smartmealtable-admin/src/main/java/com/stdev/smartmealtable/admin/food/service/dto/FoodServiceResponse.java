package com.stdev.smartmealtable.admin.food.service.dto;

import com.stdev.smartmealtable.domain.food.Food;

/**
 * 음식 응답 DTO - v2.0
 * 
 * <p>isMain, displayOrder 필드가 추가되었습니다.</p>
 */
public record FoodServiceResponse(
        Long foodId,
        String foodName,
        Long storeId,
        Long categoryId,
        String description,
        String imageUrl,
        Integer averagePrice,
        Boolean isMain, // 대표 메뉴 여부
        Integer displayOrder // 표시 순서
) {
    public static FoodServiceResponse from(Food food) {
        return new FoodServiceResponse(
                food.getFoodId(),
                food.getFoodName(),
                food.getStoreId(),
                food.getCategoryId(),
                food.getDescription(),
                food.getImageUrl(),
                food.getAveragePrice(),
                food.getIsMain(),
                food.getDisplayOrder()
        );
    }
}

package com.stdev.smartmealtable.admin.food.controller.dto;

import com.stdev.smartmealtable.admin.food.service.dto.FoodServiceResponse;

/**
 * 음식 응답 DTO - v2.0
 * 
 * <p>isMain, displayOrder 필드가 추가되었습니다.</p>
 */
public record FoodResponse(
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
    public static FoodResponse from(FoodServiceResponse serviceResponse) {
        return new FoodResponse(
                serviceResponse.foodId(),
                serviceResponse.foodName(),
                serviceResponse.storeId(),
                serviceResponse.categoryId(),
                serviceResponse.description(),
                serviceResponse.imageUrl(),
                serviceResponse.averagePrice(),
                serviceResponse.isMain(),
                serviceResponse.displayOrder()
        );
    }
}

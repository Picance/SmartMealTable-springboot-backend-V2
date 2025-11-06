package com.stdev.smartmealtable.admin.food.service.dto;

/**
 * 음식 생성 요청 DTO - v2.0
 * 
 * <p>isMain, displayOrder 필드가 추가되었습니다.</p>
 */
public record CreateFoodServiceRequest(
        String foodName,
        Long storeId,
        Long categoryId,
        String description,
        String imageUrl,
        Integer averagePrice,
        boolean isMain, // 대표 메뉴 여부
        Integer displayOrder // 표시 순서 (null이면 자동 할당)
) {
}

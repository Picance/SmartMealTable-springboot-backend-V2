package com.stdev.smartmealtable.admin.food.service.dto;

/**
 * 음식 목록 조회 요청 DTO
 */
public record FoodListServiceRequest(
        Long categoryId,
        Long storeId,
        String name,
        int page,
        int size
) {
}

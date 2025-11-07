package com.stdev.smartmealtable.api.store.dto;

import com.stdev.smartmealtable.domain.food.Food;

import java.time.LocalDateTime;

/**
 * 음식(메뉴) 정보 DTO
 */
public record FoodDto(
        Long foodId,
        String foodName,
        Integer price,
        String description,
        String imageUrl,
        Boolean isMain,
        Integer displayOrder,
        Boolean isAvailable,
        LocalDateTime registeredDt
) {
    public static FoodDto from(Food food) {
        return new FoodDto(
                food.getFoodId(),
                food.getFoodName(),
                food.getPrice(),
                food.getDescription(),
                food.getImageUrl(),
                food.getIsMain(),
                food.getDisplayOrder(),
                food.getDeletedAt() == null, // deletedAt이 null이면 판매 가능
                food.getRegisteredDt()
        );
    }
}

package com.stdev.smartmealtable.api.store.dto;

import com.stdev.smartmealtable.domain.food.Food;

import java.util.List;

/**
 * 가게별 메뉴 목록 조회 응답 DTO
 */
public record GetStoreFoodsResponse(
        Long storeId,
        String storeName,
        List<FoodDto> foods
) {
    public static GetStoreFoodsResponse of(Long storeId, String storeName, List<Food> foods) {
        return new GetStoreFoodsResponse(
                storeId,
                storeName,
                foods.stream()
                        .map(FoodDto::from)
                        .toList()
        );
    }
}

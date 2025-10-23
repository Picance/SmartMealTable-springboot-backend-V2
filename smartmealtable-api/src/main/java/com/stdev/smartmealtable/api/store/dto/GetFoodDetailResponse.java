package com.stdev.smartmealtable.api.store.dto;

import com.stdev.smartmealtable.domain.food.Food;
import com.stdev.smartmealtable.domain.store.Store;

/**
 * 메뉴 상세 조회 응답 DTO
 */
public record GetFoodDetailResponse(
        Long foodId,
        String foodName,
        String description,
        Integer price,
        String imageUrl,
        StoreInfo store,
        Boolean isAvailable,
        BudgetComparison budgetComparison
) {
    public static GetFoodDetailResponse from(
            Food food,
            Store store,
            Integer userMealBudget
    ) {
        return new GetFoodDetailResponse(
                food.getFoodId(),
                food.getFoodName(),
                food.getDescription(),
                food.getAveragePrice(),
                food.getImageUrl(),
                StoreInfo.from(store),
                true, // TODO: 음식 판매 가능 여부 확인 필요
                BudgetComparison.of(userMealBudget, food.getAveragePrice())
        );
    }

    /**
     * 가게 정보
     */
    public record StoreInfo(
            Long storeId,
            String storeName,
            String categoryName,
            String address,
            String phoneNumber,
            Integer averagePrice,
            Integer reviewCount,
            String imageUrl
    ) {
        public static StoreInfo from(Store store) {
            return new StoreInfo(
                    store.getStoreId(),
                    store.getName(),
                    null, // TODO: Category 조인 필요
                    store.getAddress(),
                    store.getPhoneNumber(),
                    store.getAveragePrice(),
                    store.getReviewCount(),
                    store.getImageUrl()
            );
        }
    }
}

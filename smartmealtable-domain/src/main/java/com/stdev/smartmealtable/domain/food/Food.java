package com.stdev.smartmealtable.domain.food;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 음식 도메인 엔티티
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Food {

    private Long foodId;
    private String foodName;
    private Long categoryId;
    private String description;
    private String imageUrl;
    private Integer averagePrice;

    /**
     * JPA Entity에서 Domain Entity로 변환 시 사용 (reconstitute 패턴)
     */
    public static Food reconstitute(
            Long foodId,
            String foodName,
            Long categoryId,
            String description,
            String imageUrl,
            Integer averagePrice
    ) {
        Food food = new Food();
        food.foodId = foodId;
        food.foodName = foodName;
        food.categoryId = categoryId;
        food.description = description;
        food.imageUrl = imageUrl;
        food.averagePrice = averagePrice;
        return food;
    }
}

package com.stdev.smartmealtable.domain.food;

import java.util.List;

/**
 * Food 페이지 결과를 나타내는 POJO
 * Spring Data의 Page를 Domain 모듈에 노출시키지 않기 위해 별도의 POJO로 분리
 */
public record FoodPageResult(
        List<Food> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    /**
     * Spring Data Page를 FoodPageResult로 변환하는 팩토리 메서드
     */
    public static FoodPageResult of(List<Food> content, int page, int size, long totalElements) {
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
        boolean hasNext = page < totalPages - 1;
        
        return new FoodPageResult(
                content,
                page,
                size,
                totalElements,
                totalPages,
                hasNext
        );
    }
}

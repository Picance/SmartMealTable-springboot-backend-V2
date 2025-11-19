package com.stdev.smartmealtable.api.food.service.dto;

import java.util.List;

/**
 * 음식 자동완성 응답 DTO
 * 
 * @param suggestions 자동완성 제안 목록
 */
public record FoodAutocompleteResponse(
    List<FoodSuggestion> suggestions,
    List<String> keywordRecommendations
) {
    
    /**
     * 자동완성 제안 항목
     * 
     * @param foodId 음식 ID
     * @param foodName 음식 이름
     * @param storeId 가게 ID
     * @param storeName 가게 이름
     * @param categoryName 카테고리 이름 (예: "한식")
     * @param averagePrice 평균 가격
     * @param isMain 대표 메뉴 여부
     */
    public record FoodSuggestion(
        Long foodId,
        String foodName,
        Long storeId,
        String storeName,
        String categoryName,
        Integer averagePrice,
        Boolean isMain
    ) {}
}

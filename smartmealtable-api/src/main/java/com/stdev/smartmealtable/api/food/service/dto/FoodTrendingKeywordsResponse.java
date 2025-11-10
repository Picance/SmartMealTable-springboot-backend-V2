package com.stdev.smartmealtable.api.food.service.dto;

import java.util.List;

/**
 * 음식 검색 인기 검색어 응답 DTO
 * 
 * @param keywords 인기 검색어 목록
 */
public record FoodTrendingKeywordsResponse(
    List<TrendingKeyword> keywords
) {
    
    /**
     * 인기 검색어 항목
     * 
     * @param keyword 검색어
     * @param searchCount 검색 횟수
     * @param rank 순위
     */
    public record TrendingKeyword(
        String keyword,
        Long searchCount,
        Integer rank
    ) {}
}

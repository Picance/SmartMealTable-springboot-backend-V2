package com.stdev.smartmealtable.api.store.service.dto;

import com.stdev.smartmealtable.domain.store.StoreType;

import java.util.List;

/**
 * 가게 자동완성 응답 DTO
 * 
 * @param suggestions 자동완성 제안 목록
 */
public record StoreAutocompleteResponse(
    List<StoreSuggestion> suggestions,
    List<String> keywordRecommendations
) {
    
    /**
     * 자동완성 제안 항목
     * 
     * @param storeId 가게 ID
     * @param name 가게 이름
     * @param storeType 가게 타입
     * @param address 주소
     * @param categoryNames 카테고리 이름 목록 (예: ["한식", "분식"])
     */
    public record StoreSuggestion(
        Long storeId,
        String name,
        StoreType storeType,
        String address,
        List<String> categoryNames
    ) {}
}

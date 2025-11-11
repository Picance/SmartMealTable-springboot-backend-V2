package com.stdev.smartmealtable.api.recommendation.service.dto;

import java.util.List;

/**
 * 통합 자동완성 응답 DTO
 *
 * 기능:
 * 1. 키워드 섹션: 음식명 + 가게명을 섞어서 제공
 * 2. 가게 바로가기 섹션: 검색 결과의 가게들에 대한 상세 정보
 *
 * @param suggestions 자동완성 키워드 목록 (음식명 + 가게명 혼합)
 * @param storeShortcuts 가게 바로가기 목록 (가게 ID, 이름, 대표 이미지, 영업 상태 등)
 */
public record UnifiedAutocompleteResponse(
    List<String> suggestions,
    List<StoreShortcut> storeShortcuts
) {

    /**
     * 가게 바로가기 정보
     *
     * @param storeId 가게 ID
     * @param name 가게 이름
     * @param imageUrl 대표 이미지 URL
     * @param isOpen 영업 중 여부
     */
    public record StoreShortcut(
        Long storeId,
        String name,
        String imageUrl,
        Boolean isOpen
    ) {}
}

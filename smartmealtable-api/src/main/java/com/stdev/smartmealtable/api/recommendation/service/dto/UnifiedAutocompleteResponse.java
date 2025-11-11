package com.stdev.smartmealtable.api.recommendation.service.dto;

import java.util.List;

/**
 * 통합 자동완성 응답 DTO
 * 음식명과 가게명을 함께 제공
 *
 * @param suggestions 자동완성 키워드 목록 (음식명 + 가게명)
 */
public record UnifiedAutocompleteResponse(
    List<String> suggestions
) {}

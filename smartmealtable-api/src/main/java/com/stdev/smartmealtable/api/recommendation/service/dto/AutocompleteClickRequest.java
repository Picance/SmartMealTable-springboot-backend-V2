package com.stdev.smartmealtable.api.recommendation.service.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 자동완성 클릭 이벤트 요청 DTO
 */
public record AutocompleteClickRequest(
        @NotBlank(message = "keyword는 필수입니다.")
        String keyword,
        Long foodId,
        Double latitude,
        Double longitude
) {
}

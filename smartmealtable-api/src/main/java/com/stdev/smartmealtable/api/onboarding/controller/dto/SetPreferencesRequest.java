package com.stdev.smartmealtable.api.onboarding.controller.dto;

import com.stdev.smartmealtable.domain.member.entity.RecommendationType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 취향 설정 요청 DTO
 */
@Getter
@RequiredArgsConstructor
public class SetPreferencesRequest {

    @NotNull(message = "추천 유형은 필수입니다.")
    private final RecommendationType recommendationType;

    @NotEmpty(message = "선호도 정보는 최소 1개 이상이어야 합니다.")
    @Valid
    private final List<PreferenceItem> preferences;

    @Getter
    @RequiredArgsConstructor
    public static class PreferenceItem {

        @NotNull(message = "카테고리 ID는 필수입니다.")
        private final Long categoryId;

        @NotNull(message = "선호도 가중치는 필수입니다.")
        private final Integer weight;
    }
}

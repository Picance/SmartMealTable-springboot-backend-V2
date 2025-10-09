package com.stdev.smartmealtable.api.onboarding.service.dto;

import com.stdev.smartmealtable.domain.member.entity.RecommendationType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 취향 설정 응답 DTO
 */
@Getter
@RequiredArgsConstructor
public class SetPreferencesServiceResponse {

    private final RecommendationType recommendationType;
    private final List<PreferenceInfo> preferences;

    @Getter
    @RequiredArgsConstructor
    public static class PreferenceInfo {
        private final Long categoryId;
        private final String categoryName;
        private final Integer weight;
    }
}

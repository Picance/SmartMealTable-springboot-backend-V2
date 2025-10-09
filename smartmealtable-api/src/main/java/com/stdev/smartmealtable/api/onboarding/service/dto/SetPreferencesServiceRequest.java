package com.stdev.smartmealtable.api.onboarding.service.dto;

import com.stdev.smartmealtable.domain.member.entity.RecommendationType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 취향 설정 요청 DTO
 */
@Getter
@RequiredArgsConstructor
public class SetPreferencesServiceRequest {

    private final RecommendationType recommendationType;
    private final List<PreferenceItem> preferences;

    @Getter
    @RequiredArgsConstructor
    public static class PreferenceItem {
        private final Long categoryId;
        private final Integer weight;
    }
}

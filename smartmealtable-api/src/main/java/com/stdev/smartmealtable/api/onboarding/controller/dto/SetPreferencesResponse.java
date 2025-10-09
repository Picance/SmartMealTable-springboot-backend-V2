package com.stdev.smartmealtable.api.onboarding.controller.dto;

import com.stdev.smartmealtable.api.onboarding.service.dto.SetPreferencesServiceResponse;
import com.stdev.smartmealtable.domain.member.entity.RecommendationType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 취향 설정 응답 DTO
 */
@Getter
@RequiredArgsConstructor
public class SetPreferencesResponse {

    private final RecommendationType recommendationType;
    private final List<PreferenceInfo> preferences;

    @Getter
    @RequiredArgsConstructor
    public static class PreferenceInfo {
        private final Long categoryId;
        private final String categoryName;
        private final Integer weight;
    }

    /**
     * Service DTO → Controller DTO 변환
     */
    public static SetPreferencesResponse from(SetPreferencesServiceResponse serviceResponse) {
        List<PreferenceInfo> preferenceInfos = serviceResponse.getPreferences().stream()
            .map(p -> new PreferenceInfo(p.getCategoryId(), p.getCategoryName(), p.getWeight()))
            .collect(Collectors.toList());

        return new SetPreferencesResponse(
            serviceResponse.getRecommendationType(),
            preferenceInfos
        );
    }
}

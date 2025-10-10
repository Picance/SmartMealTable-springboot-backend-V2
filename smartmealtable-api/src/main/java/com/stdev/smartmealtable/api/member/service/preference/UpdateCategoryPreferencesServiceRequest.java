package com.stdev.smartmealtable.api.member.service.preference;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * PUT /api/v1/members/me/preferences/categories - 카테고리 선호도 수정 Service Request
 */
@Getter
@NoArgsConstructor
public class UpdateCategoryPreferencesServiceRequest {

    private List<CategoryPreferenceItem> preferences;

    public UpdateCategoryPreferencesServiceRequest(List<CategoryPreferenceItem> preferences) {
        validatePreferences(preferences);
        this.preferences = preferences;
    }

    private void validatePreferences(List<CategoryPreferenceItem> preferences) {
        if (preferences == null || preferences.isEmpty()) {
            throw new IllegalArgumentException("선호도 정보는 최소 1개 이상이어야 합니다.");
        }
    }

    @Getter
    @NoArgsConstructor
    public static class CategoryPreferenceItem {
        private Long categoryId;
        private Integer weight;

        public CategoryPreferenceItem(Long categoryId, Integer weight) {
            this.categoryId = categoryId;
            this.weight = weight;
        }
    }
}

package com.stdev.smartmealtable.api.member.controller.preference;

import com.stdev.smartmealtable.api.member.service.preference.UpdateCategoryPreferencesServiceRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * PUT /api/v1/members/me/preferences/categories - 카테고리 선호도 수정 Request
 */
@Getter
@NoArgsConstructor
public class UpdateCategoryPreferencesRequest {

    @NotEmpty(message = "선호도 정보는 최소 1개 이상이어야 합니다.")
    private List<@Valid CategoryPreferenceItem> preferences;

    public UpdateCategoryPreferencesRequest(List<CategoryPreferenceItem> preferences) {
        this.preferences = preferences;
    }

    // Service Request로 변환
    public UpdateCategoryPreferencesServiceRequest toServiceRequest() {
        List<UpdateCategoryPreferencesServiceRequest.CategoryPreferenceItem> items = preferences.stream()
                .map(p -> new UpdateCategoryPreferencesServiceRequest.CategoryPreferenceItem(p.getCategoryId(), p.getWeight()))
                .toList();
        return new UpdateCategoryPreferencesServiceRequest(items);
    }

    @Getter
    @NoArgsConstructor
    public static class CategoryPreferenceItem {
        @NotNull(message = "카테고리 ID는 필수입니다.")
        private Long categoryId;

        @NotNull(message = "가중치는 필수입니다.")
        private Integer weight;

        public CategoryPreferenceItem(Long categoryId, Integer weight) {
            this.categoryId = categoryId;
            this.weight = weight;
        }
    }
}

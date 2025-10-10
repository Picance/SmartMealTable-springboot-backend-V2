package com.stdev.smartmealtable.api.member.service.preference;

import com.stdev.smartmealtable.domain.member.entity.RecommendationType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 선호도 조회 응답 DTO
 */
@Getter
@Builder
public class GetPreferencesServiceResponse {

    private RecommendationType recommendationType;
    private List<CategoryPreferenceDto> categoryPreferences;
    private FoodPreferencesDto foodPreferences;

    @Getter
    @Builder
    public static class CategoryPreferenceDto {
        private Long preferenceId;
        private Long categoryId;
        private String categoryName;
        private Integer weight;
    }

    @Getter
    @Builder
    public static class FoodPreferencesDto {
        private List<FoodPreferenceItemDto> liked;
        private List<FoodPreferenceItemDto> disliked;
    }

    @Getter
    @Builder
    public static class FoodPreferenceItemDto {
        private Long foodPreferenceId;
        private Long foodId;
        private String foodName;
        private String categoryName;
        private LocalDateTime createdAt;
    }
}

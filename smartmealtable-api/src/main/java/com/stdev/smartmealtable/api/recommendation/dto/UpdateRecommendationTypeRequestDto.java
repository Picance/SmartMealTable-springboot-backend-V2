package com.stdev.smartmealtable.api.recommendation.dto;

import com.stdev.smartmealtable.domain.member.entity.RecommendationType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 추천 유형 변경 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRecommendationTypeRequestDto {

    @NotNull(message = "추천 유형은 필수입니다.")
    private RecommendationType recommendationType;
}

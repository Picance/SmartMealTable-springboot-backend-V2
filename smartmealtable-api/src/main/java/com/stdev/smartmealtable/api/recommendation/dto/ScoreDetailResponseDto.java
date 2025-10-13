package com.stdev.smartmealtable.api.recommendation.dto;

import com.stcom.smartmealtable.recommendation.domain.model.ScoreDetail;
import com.stdev.smartmealtable.domain.member.entity.RecommendationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 점수 상세 조회 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoreDetailResponseDto {

    /**
     * 가게 ID
     */
    private Long storeId;

    /**
     * 가게명
     */
    private String storeName;

    /**
     * 사용자 추천 유형
     */
    private RecommendationType recommendationType;

    /**
     * 최종 추천 점수 (0-100)
     */
    private Double finalScore;

    /**
     * 안정성 점수 (0-100)
     */
    private Double stabilityScore;

    /**
     * 탐험성 점수 (0-100)
     */
    private Double explorationScore;

    /**
     * 예산 효율성 점수 (0-100)
     */
    private Double budgetEfficiencyScore;

    /**
     * 접근성 점수 (0-100)
     */
    private Double accessibilityScore;

    /**
     * 안정성 가중 점수
     */
    private Double weightedStabilityScore;

    /**
     * 탐험성 가중 점수
     */
    private Double weightedExplorationScore;

    /**
     * 예산 효율성 가중 점수
     */
    private Double weightedBudgetEfficiencyScore;

    /**
     * 접근성 가중 점수
     */
    private Double weightedAccessibilityScore;

    /**
     * 거리 (km)
     */
    private Double distance;

    /**
     * Domain 모델로부터 변환
     */
    public static ScoreDetailResponseDto from(ScoreDetail scoreDetail, Long storeId, String storeName, Double distance) {
        return ScoreDetailResponseDto.builder()
                .storeId(storeId)
                .storeName(storeName)
                .recommendationType(scoreDetail.getRecommendationType())
                .finalScore(scoreDetail.getFinalScore())
                .stabilityScore(scoreDetail.getStabilityScore())
                .explorationScore(scoreDetail.getExplorationScore())
                .budgetEfficiencyScore(scoreDetail.getBudgetEfficiencyScore())
                .accessibilityScore(scoreDetail.getAccessibilityScore())
                .weightedStabilityScore(scoreDetail.getWeightedStabilityScore())
                .weightedExplorationScore(scoreDetail.getWeightedExplorationScore())
                .weightedBudgetEfficiencyScore(scoreDetail.getWeightedBudgetEfficiencyScore())
                .weightedAccessibilityScore(scoreDetail.getWeightedAccessibilityScore())
                .distance(distance)
                .build();
    }
}

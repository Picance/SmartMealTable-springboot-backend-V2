package com.stcom.smartmealtable.recommendation.domain.model;

import com.stdev.smartmealtable.domain.member.entity.RecommendationType;
import lombok.Builder;
import lombok.Getter;

/**
 * 점수 상세 정보
 * 
 * <p>4가지 속성 점수와 가중치, 최종 점수를 포함합니다.</p>
 */
@Getter
@Builder
public class ScoreDetail {
    
    /**
     * 사용자 추천 유형
     */
    private final RecommendationType recommendationType;
    
    /**
     * 최종 추천 점수
     */
    private final double finalScore;
    
    /**
     * 안정성 점수
     */
    private final double stabilityScore;
    
    /**
     * 탐험성 점수
     */
    private final double explorationScore;
    
    /**
     * 예산 효율성 점수
     */
    private final double budgetEfficiencyScore;
    
    /**
     * 접근성 점수
     */
    private final double accessibilityScore;
    
    /**
     * 안정성 가중 점수 (점수 * 가중치)
     */
    private final double weightedStabilityScore;
    
    /**
     * 탐험성 가중 점수
     */
    private final double weightedExplorationScore;
    
    /**
     * 예산 효율성 가중 점수
     */
    private final double weightedBudgetEfficiencyScore;
    
    /**
     * 접근성 가중 점수
     */
    private final double weightedAccessibilityScore;
}

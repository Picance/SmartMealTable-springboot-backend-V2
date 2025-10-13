package com.stcom.smartmealtable.recommendation.domain.service;

import com.stcom.smartmealtable.recommendation.domain.calculator.AccessibilityScoreCalculator;
import com.stcom.smartmealtable.recommendation.domain.calculator.BudgetEfficiencyScoreCalculator;
import com.stcom.smartmealtable.recommendation.domain.calculator.ExplorationScoreCalculator;
import com.stcom.smartmealtable.recommendation.domain.calculator.StabilityScoreCalculator;
import com.stcom.smartmealtable.recommendation.domain.model.CalculationContext;
import com.stcom.smartmealtable.recommendation.domain.model.RecommendationResult;
import com.stcom.smartmealtable.recommendation.domain.model.ScoreDetail;
import com.stcom.smartmealtable.recommendation.domain.model.UserProfile;
import com.stdev.smartmealtable.domain.member.entity.RecommendationType;
import com.stdev.smartmealtable.domain.store.Store;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 추천 도메인 서비스
 * 
 * <p>4가지 속성 점수를 계산하고 가중합하여 최종 추천 점수를 산출합니다.</p>
 */
@Service
@RequiredArgsConstructor
public class RecommendationDomainService {

    private final StabilityScoreCalculator stabilityCalculator = new StabilityScoreCalculator();
    private final ExplorationScoreCalculator explorationCalculator = new ExplorationScoreCalculator();
    private final BudgetEfficiencyScoreCalculator budgetEfficiencyCalculator = new BudgetEfficiencyScoreCalculator();
    private final AccessibilityScoreCalculator accessibilityCalculator = new AccessibilityScoreCalculator();

    /**
     * 가게 목록에 대한 추천 점수를 계산합니다.
     * 
     * @param stores 가게 목록
     * @param userProfile 사용자 프로필
     * @return 추천 결과 목록
     */
    public List<RecommendationResult> calculateRecommendations(
            List<Store> stores,
            UserProfile userProfile
    ) {
        // 계산 컨텍스트 생성
        CalculationContext context = CalculationContext.from(stores, userProfile);

        // 각 가게에 대해 점수 계산
        return stores.stream()
                .map(store -> calculateSingleRecommendation(store, userProfile, context))
                .collect(Collectors.toList());
    }

    /**
     * 단일 가게에 대한 추천 점수 계산
     * 
     * @param store 가게
     * @param userProfile 사용자 프로필
     * @param context 계산 컨텍스트
     * @return 추천 결과
     */
    private RecommendationResult calculateSingleRecommendation(
            Store store,
            UserProfile userProfile,
            CalculationContext context
    ) {
        // 4가지 속성 점수 계산
        double stabilityScore = stabilityCalculator.calculate(store, userProfile, context);
        double explorationScore = explorationCalculator.calculate(store, userProfile, context);
        double budgetEfficiencyScore = budgetEfficiencyCalculator.calculate(store, userProfile, context);
        double accessibilityScore = accessibilityCalculator.calculate(store, userProfile, context);

        // 사용자 유형별 가중치 적용
        RecommendationType type = userProfile.getRecommendationType();
        double finalScore = calculateFinalScore(
                type,
                stabilityScore,
                explorationScore,
                budgetEfficiencyScore,
                accessibilityScore
        );

        // 거리 계산
        double distance = calculateDistance(
                userProfile.getCurrentLatitude(),
                userProfile.getCurrentLongitude(),
                store.getLatitude(),
                store.getLongitude()
        );

        // 점수 상세 정보 생성
        ScoreDetail scoreDetail = createScoreDetail(
                type,
                finalScore,
                stabilityScore,
                explorationScore,
                budgetEfficiencyScore,
                accessibilityScore
        );

        return RecommendationResult.from(store, finalScore, distance, scoreDetail);
    }

    /**
     * 사용자 유형별 최종 점수 계산
     */
    private double calculateFinalScore(
            RecommendationType type,
            double stability,
            double exploration,
            double budgetEfficiency,
            double accessibility
    ) {
        return switch (type) {
            case SAVER -> // 절약형: 예산효율성 50%, 안정성 30%, 탐험성 15%, 접근성 5%
                    (stability * 0.30) +
                    (exploration * 0.15) +
                    (budgetEfficiency * 0.50) +
                    (accessibility * 0.05);
            
            case ADVENTURER -> // 모험형: 탐험성 50%, 안정성 30%, 예산효율성 10%, 접근성 10%
                    (stability * 0.30) +
                    (exploration * 0.50) +
                    (budgetEfficiency * 0.10) +
                    (accessibility * 0.10);
            
            case BALANCED -> // 균형형: 안정성 30%, 탐험성 25%, 예산효율성 30%, 접근성 15%
                    (stability * 0.30) +
                    (exploration * 0.25) +
                    (budgetEfficiency * 0.30) +
                    (accessibility * 0.15);
        };
    }

    /**
     * 점수 상세 정보 생성
     */
    private ScoreDetail createScoreDetail(
            RecommendationType type,
            double finalScore,
            double stability,
            double exploration,
            double budgetEfficiency,
            double accessibility
    ) {
        // 가중치 가져오기
        double[] weights = getWeights(type);
        
        return ScoreDetail.builder()
                .recommendationType(type)
                .finalScore(finalScore)
                .stabilityScore(stability)
                .explorationScore(exploration)
                .budgetEfficiencyScore(budgetEfficiency)
                .accessibilityScore(accessibility)
                .weightedStabilityScore(stability * weights[0])
                .weightedExplorationScore(exploration * weights[1])
                .weightedBudgetEfficiencyScore(budgetEfficiency * weights[2])
                .weightedAccessibilityScore(accessibility * weights[3])
                .build();
    }

    /**
     * 사용자 유형별 가중치 배열 반환
     */
    private double[] getWeights(RecommendationType type) {
        return switch (type) {
            case SAVER -> new double[]{0.30, 0.15, 0.50, 0.05};
            case ADVENTURER -> new double[]{0.30, 0.50, 0.10, 0.10};
            case BALANCED -> new double[]{0.30, 0.25, 0.30, 0.15};
        };
    }

    /**
     * Haversine 공식을 사용한 거리 계산
     */
    private double calculateDistance(
            BigDecimal lat1, BigDecimal lon1,
            BigDecimal lat2, BigDecimal lon2
    ) {
        final int EARTH_RADIUS = 6371; // km

        double dLat = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double dLon = Math.toRadians(lon2.doubleValue() - lon1.doubleValue());

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1.doubleValue())) *
                   Math.cos(Math.toRadians(lat2.doubleValue())) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }
}

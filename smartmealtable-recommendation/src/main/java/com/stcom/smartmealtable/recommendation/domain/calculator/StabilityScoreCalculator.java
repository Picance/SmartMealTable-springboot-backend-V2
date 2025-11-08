package com.stcom.smartmealtable.recommendation.domain.calculator;

import com.stcom.smartmealtable.recommendation.domain.model.CalculationContext;
import com.stcom.smartmealtable.recommendation.domain.model.ExpenditureRecord;
import com.stcom.smartmealtable.recommendation.domain.model.UserProfile;
import com.stcom.smartmealtable.recommendation.util.NormalizationUtil;
import com.stdev.smartmealtable.domain.store.Store;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;

/**
 * 안정성 점수 계산기
 * 
 * <p>사용자의 선호도, 과거 지출 내역, 리뷰 신뢰도를 기반으로 점수를 계산합니다.</p>
 * 
 * <ul>
 *   <li>선호 카테고리 (40%)</li>
 *   <li>과거 지출 기록 (40%)</li>
 *   <li>리뷰 신뢰도 (20%)</li>
 * </ul>
 */
public class StabilityScoreCalculator implements ScoreCalculator {

    private static final double PREFERENCE_WEIGHT = 0.4;      // 40%
    private static final double EXPENDITURE_WEIGHT = 0.4;     // 40%
    private static final double REVIEW_WEIGHT = 0.2;          // 20%
    private static final double TIME_DECAY_LAMBDA = 0.01;     // 시간 감쇠 상수
    private static final int MINIMUM_EXPENDITURE_COUNT = 3;   // 최소 지출 내역 개수

    @Override
    public double calculate(Store store, UserProfile userProfile, CalculationContext context) {
        double preferenceScore = calculatePreferenceScore(store, userProfile);
        double expenditureScore = calculateExpenditureScore(store, userProfile);
        double reviewScore = calculateReviewScore(store, context);

        return (preferenceScore * PREFERENCE_WEIGHT) +
               (expenditureScore * EXPENDITURE_WEIGHT) +
               (reviewScore * REVIEW_WEIGHT);
    }

    /**
     * 선호 카테고리 점수 계산
     * 
     * <p>가게가 여러 카테고리를 가진 경우, 주 카테고리(첫 번째)를 사용합니다.</p>
     * 
     * @param store 가게
     * @param userProfile 사용자 프로필
     * @return 0~100 점수
     */
    private double calculatePreferenceScore(Store store, UserProfile userProfile) {
        // 주 카테고리 ID (첫 번째 카테고리 사용)
        Long primaryCategoryId = store.getCategoryIds() != null && !store.getCategoryIds().isEmpty() 
                ? store.getCategoryIds().get(0) 
                : null;

        if (primaryCategoryId == null) {
            return 50.0; // 카테고리가 없으면 중간 점수
        }

        // 카테고리 선호도: 100 (좋아요) / 0 (보통) / -100 (싫어요)
        Integer categoryWeight = userProfile.getCategoryPreference(primaryCategoryId);
        
        // -100~100 범위를 0~100으로 정규화
        return NormalizationUtil.normalize(categoryWeight, -100, 100);
    }

    /**
     * 과거 지출 기록 점수 계산 (시간 감쇠 적용)
     * 
     * <p>가게가 여러 카테고리를 가진 경우, 주 카테고리(첫 번째)를 사용합니다.</p>
     * 
     * @param store 가게
     * @param userProfile 사용자 프로필
     * @return 0~100 점수
     */
    private double calculateExpenditureScore(Store store, UserProfile userProfile) {
        // 주 카테고리 ID (첫 번째 카테고리 사용)
        Long primaryCategoryId = store.getCategoryIds() != null && !store.getCategoryIds().isEmpty() 
                ? store.getCategoryIds().get(0) 
                : null;

        if (primaryCategoryId == null) {
            return 0.0; // 카테고리가 없으면 0점
        }

        // 최근 6개월 지출 내역 조회
        Map<LocalDate, ExpenditureRecord> recentExpenditures = userProfile.getRecentExpenditures(180);

        // 신규 사용자 처리 (지출 내역 3건 미만)
        if (recentExpenditures.size() < MINIMUM_EXPENDITURE_COUNT) {
            return 0.0; // 가중치 0으로 처리
        }

        double totalWeightedAmount = 0.0;
        double categoryWeightedAmount = 0.0;
        LocalDate now = LocalDate.now();

        for (Map.Entry<LocalDate, ExpenditureRecord> entry : recentExpenditures.entrySet()) {
            ExpenditureRecord exp = entry.getValue();
            
            // 시간 감쇠 가중치 계산: w = exp(-λ * days_ago)
            long daysAgo = ChronoUnit.DAYS.between(exp.getExpendedAt(), now);
            double weight = Math.exp(-TIME_DECAY_LAMBDA * daysAgo);
            double weightedAmount = exp.getAmount() * weight;

            totalWeightedAmount += weightedAmount;
            
            // 같은 카테고리인 경우만 합산
            if (exp.getCategoryId().equals(primaryCategoryId)) {
                categoryWeightedAmount += weightedAmount;
            }
        }

        // 전체 지출 대비 해당 카테고리 비중 계산
        if (totalWeightedAmount == 0) {
            return 0.0;
        }

        double categoryProportion = categoryWeightedAmount / totalWeightedAmount;
        return categoryProportion * 100; // 비율을 0~100 점수로 변환
    }

    /**
     * 리뷰 신뢰도 점수 계산
     * 
     * @param store 가게
     * @param context 계산 컨텍스트
     * @return 0~100 점수
     */
    private double calculateReviewScore(Store store, CalculationContext context) {
        // 전체 가게들의 리뷰 수 중 상대적 위치
        double reviewCount = store.getReviewCount() != null ? store.getReviewCount() : 0;
        return NormalizationUtil.normalizeMinMax(reviewCount, context.getMinReviews(), context.getMaxReviews());
    }
}

package com.stcom.smartmealtable.recommendation.domain.calculator;

import com.stcom.smartmealtable.recommendation.domain.model.CalculationContext;
import com.stcom.smartmealtable.recommendation.domain.model.UserProfile;
import com.stcom.smartmealtable.recommendation.util.NormalizationUtil;
import com.stdev.smartmealtable.domain.store.Store;

/**
 * 예산 효율성 점수 계산기
 * 
 * <p>가성비와 예산 적합성을 기반으로 점수를 계산합니다.</p>
 * 
 * <ul>
 *   <li>가성비 (60%): 가격 대비 리뷰 수</li>
 *   <li>예산 적합성 (40%): 사용자 예산과의 차이</li>
 * </ul>
 */
public class BudgetEfficiencyScoreCalculator implements ScoreCalculator {

    private static final double VALUE_FOR_MONEY_WEIGHT = 0.6;  // 60%
    private static final double BUDGET_FIT_WEIGHT = 0.4;       // 40%

    @Override
    public double calculate(Store store, UserProfile userProfile, CalculationContext context) {
        double valueScore = calculateValueForMoneyScore(store, context);
        double budgetFitScore = calculateBudgetFitScore(store);

        return (valueScore * VALUE_FOR_MONEY_WEIGHT) +
               (budgetFitScore * BUDGET_FIT_WEIGHT);
    }

    /**
     * 가성비 점수 계산
     * 
     * <p>가성비 = log(1 + reviews) / avg_price</p>
     * 
     * @param store 가게
     * @param context 계산 컨텍스트
     * @return 0~100 점수
     */
    private double calculateValueForMoneyScore(Store store, CalculationContext context) {
        int reviewCount = store.getReviewCount() != null ? store.getReviewCount() : 0;
        int avgPrice = store.getAveragePrice() != null && store.getAveragePrice() > 0 
                       ? store.getAveragePrice() 
                       : 1; // 0으로 나누기 방지

        double valueForMoney = Math.log(1 + reviewCount) / avgPrice;

        return NormalizationUtil.normalizeMinMax(
                valueForMoney,
                context.getMinValueForMoney(),
                context.getMaxValueForMoney()
        );
    }

    /**
     * 예산 적합성 점수 계산
     * 
     * <p>사용자 예산과 가게 평균 가격의 차이를 계산합니다.</p>
     * <p>현재는 간소화하여 일정 범위 내에서 점수를 부여합니다.</p>
     * 
     * @param store 가게
     * @return 0~100 점수
     */
    private double calculateBudgetFitScore(Store store) {
        // TODO: UserProfile에서 현재 시간대에 맞는 예산 가져오기 (추후 구현)
        // 현재는 평균 가격이 저렴할수록 높은 점수로 간단히 처리
        
        int avgPrice = store.getAveragePrice() != null ? store.getAveragePrice() : 0;
        
        // 가격이 낮을수록 높은 점수 (간소화된 버전)
        // 10,000원 이하는 100점, 20,000원 이상은 0점으로 선형 변환
        if (avgPrice <= 10000) {
            return 100.0;
        } else if (avgPrice >= 20000) {
            return 0.0;
        } else {
            return 100.0 - ((avgPrice - 10000) / 100.0);
        }
    }
}

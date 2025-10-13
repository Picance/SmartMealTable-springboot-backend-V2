package com.stcom.smartmealtable.recommendation.domain.calculator;

import com.stcom.smartmealtable.recommendation.domain.model.CalculationContext;
import com.stcom.smartmealtable.recommendation.domain.model.UserProfile;
import com.stdev.smartmealtable.domain.store.Store;

/**
 * 점수 계산 전략 인터페이스
 * 
 * <p>Strategy 패턴을 사용하여 각 속성별 점수 계산 로직을 분리합니다.</p>
 */
public interface ScoreCalculator {
    
    /**
     * 특정 가게에 대한 점수를 계산합니다.
     * 
     * @param store 점수를 계산할 가게
     * @param userProfile 사용자 프로필
     * @param context 계산에 필요한 추가 컨텍스트 (전체 가게 리스트, 통계 등)
     * @return 0~100 사이의 정규화된 점수
     */
    double calculate(Store store, UserProfile userProfile, CalculationContext context);
}

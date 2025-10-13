package com.stcom.smartmealtable.recommendation.domain.calculator;

import com.stcom.smartmealtable.recommendation.domain.model.CalculationContext;
import com.stcom.smartmealtable.recommendation.domain.model.UserProfile;
import com.stcom.smartmealtable.recommendation.util.NormalizationUtil;
import com.stdev.smartmealtable.domain.store.Store;

/**
 * 접근성 점수 계산기
 * 
 * <p>사용자의 현재 위치와 가게 사이의 거리를 기반으로 점수를 계산합니다.</p>
 * <p>거리가 가까울수록 높은 점수를 부여합니다.</p>
 */
public class AccessibilityScoreCalculator implements ScoreCalculator {

    private static final int EARTH_RADIUS = 6371; // km

    @Override
    public double calculate(Store store, UserProfile userProfile, CalculationContext context) {
        // 사용자 현재 위치와 가게 사이의 거리 계산
        double distance = calculateDistance(
                userProfile.getCurrentLatitude().doubleValue(),
                userProfile.getCurrentLongitude().doubleValue(),
                store.getLatitude().doubleValue(),
                store.getLongitude().doubleValue()
        );

        // 거리 정규화
        double normalizedDistance = NormalizationUtil.normalizeMinMax(
                distance,
                context.getMinDistance(),
                context.getMaxDistance()
        );

        // 거리가 가까울수록 높은 점수 (역수 변환)
        return 100 - normalizedDistance;
    }

    /**
     * Haversine 공식을 사용한 두 좌표 간 거리 계산 (km 단위)
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }
}

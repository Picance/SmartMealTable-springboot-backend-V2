package com.stdev.smartmealtable.support.location;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 두 좌표 간의 거리를 계산하는 유틸리티
 *
 * Haversine 공식을 사용하여 지구 표면의 두 점 사이의 대원거리를 계산합니다.
 * 정확도: 약 0.5%
 *
 * @author SmartMealTable Team
 * @since 2025-11-12
 */
@Component
@Slf4j
public class DistanceCalculator {

    private static final double EARTH_RADIUS_KM = 6371.0; // 지구 반지름 (킬로미터)
    private static final int PRECISION_SCALE = 2; // 소수점 자리수

    /**
     * 두 좌표 간의 거리를 킬로미터 단위로 계산
     *
     * @param lat1 시작점 위도 (-90 ~ 90)
     * @param lon1 시작점 경도 (-180 ~ 180)
     * @param lat2 종료점 위도 (-90 ~ 90)
     * @param lon2 종료점 경도 (-180 ~ 180)
     * @return 거리 (킬로미터)
     * @throws IllegalArgumentException 좌표가 유효하지 않은 경우
     */
    public BigDecimal calculateDistanceKm(
            Double lat1,
            Double lon1,
            Double lat2,
            Double lon2
    ) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            throw new IllegalArgumentException("좌표 정보가 누락되었습니다");
        }

        if (!isValidLatitude(lat1) || !isValidLatitude(lat2)) {
            throw new IllegalArgumentException("위도는 -90 ~ 90 범위여야 합니다");
        }

        if (!isValidLongitude(lon1) || !isValidLongitude(lon2)) {
            throw new IllegalArgumentException("경도는 -180 ~ 180 범위여야 합니다");
        }

        // 같은 좌표인 경우
        if (lat1.equals(lat2) && lon1.equals(lon2)) {
            return BigDecimal.ZERO;
        }

        double latDiff = Math.toRadians(lat2 - lat1);
        double lonDiff = Math.toRadians(lon2 - lon1);

        double sinLat = Math.sin(latDiff / 2);
        double sinLon = Math.sin(lonDiff / 2);

        double a = sinLat * sinLat
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * sinLon * sinLon;

        double c = 2 * Math.asin(Math.sqrt(a));
        double distanceKm = EARTH_RADIUS_KM * c;

        return new BigDecimal(distanceKm).setScale(PRECISION_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * 거리를 사람이 읽기 쉬운 텍스트로 변환
     *
     * @param distanceKm 거리 (킬로미터)
     * @return 포맷된 거리 텍스트 (예: "1.5km", "100m")
     */
    public String formatDistance(BigDecimal distanceKm) {
        if (distanceKm == null) {
            return "알 수 없음";
        }

        if (distanceKm.compareTo(BigDecimal.ONE) < 0) {
            // 1km 미만: 미터 단위로 표시
            int meters = distanceKm.multiply(BigDecimal.valueOf(1000))
                    .setScale(0, RoundingMode.HALF_UP)
                    .intValue();
            return meters + "m";
        } else {
            // 1km 이상: 킬로미터 단위로 표시
            return distanceKm.toPlainString() + "km";
        }
    }

    /**
     * 거리별 분류
     *
     * @param distanceKm 거리 (킬로미터)
     * @return 거리 분류 ("매우 가까움", "가까움", "중간", "먼", "매우 멈")
     */
    public String classifyDistance(BigDecimal distanceKm) {
        if (distanceKm == null) {
            return "알 수 없음";
        }

        if (distanceKm.compareTo(new BigDecimal("0.5")) <= 0) {
            return "매우 가까움";
        } else if (distanceKm.compareTo(new BigDecimal("1")) <= 0) {
            return "가까움";
        } else if (distanceKm.compareTo(new BigDecimal("3")) <= 0) {
            return "중간";
        } else if (distanceKm.compareTo(new BigDecimal("5")) <= 0) {
            return "먼";
        } else {
            return "매우 먼";
        }
    }

    /**
     * 위도 유효성 검증
     */
    private boolean isValidLatitude(Double latitude) {
        return latitude >= -90 && latitude <= 90;
    }

    /**
     * 경도 유효성 검증
     */
    private boolean isValidLongitude(Double longitude) {
        return longitude >= -180 && longitude <= 180;
    }
}

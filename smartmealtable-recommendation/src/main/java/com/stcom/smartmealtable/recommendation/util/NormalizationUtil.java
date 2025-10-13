package com.stcom.smartmealtable.recommendation.util;

/**
 * 추천 시스템에서 사용하는 정규화 유틸리티
 * 
 * <p>서로 다른 척도의 데이터를 0~100 사이의 값으로 정규화하여
 * 일관된 점수 계산을 가능하게 합니다.</p>
 */
public final class NormalizationUtil {

    private NormalizationUtil() {
        throw new AssertionError("유틸리티 클래스는 인스턴스화할 수 없습니다.");
    }

    /**
     * Min-Max 정규화 (0~100 스케일)
     * 
     * @param value 정규화할 값
     * @param min 최소값
     * @param max 최대값
     * @return 0~100 사이의 정규화된 값
     */
    public static double normalizeMinMax(double value, double min, double max) {
        // 모든 값이 같을 경우 중립 점수 반환
        if (max == min) {
            return 50.0;
        }
        return 100.0 * (value - min) / (max - min);
    }

    /**
     * 로그 스케일 정규화
     * 
     * <p>편차가 큰 데이터(예: 지출 내역, 조회수)를 정규화할 때 사용합니다.
     * 로그 변환을 통해 큰 값의 영향력을 완화합니다.</p>
     * 
     * @param value 정규화할 값
     * @param min 최소값
     * @param max 최대값
     * @return 0~100 사이의 정규화된 값
     */
    public static double normalizeLog(double value, double min, double max) {
        double logValue = Math.log(1 + value);
        double logMin = Math.log(1 + min);
        double logMax = Math.log(1 + max);
        return normalizeMinMax(logValue, logMin, logMax);
    }

    /**
     * 선형 정규화 (임의 범위 → 0~100)
     * 
     * <p>카테고리 선호도와 같이 음수를 포함한 범위를 0~100으로 변환합니다.</p>
     * 
     * @param value 정규화할 값
     * @param min 원본 범위의 최소값
     * @param max 원본 범위의 최대값
     * @return 0~100 사이의 정규화된 값
     */
    public static double normalize(double value, double min, double max) {
        return normalizeMinMax(value, min, max);
    }
}

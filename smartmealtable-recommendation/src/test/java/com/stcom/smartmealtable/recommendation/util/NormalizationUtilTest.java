package com.stcom.smartmealtable.recommendation.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 정규화 유틸리티 테스트
 */
@DisplayName("정규화 유틸리티 테스트")
class NormalizationUtilTest {

    @Test
    @DisplayName("Min-Max 정규화 - 최소값")
    void normalizeMinMax_MinValue() {
        // when
        double result = NormalizationUtil.normalizeMinMax(0, 0, 100);

        // then
        assertThat(result).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Min-Max 정규화 - 최대값")
    void normalizeMinMax_MaxValue() {
        // when
        double result = NormalizationUtil.normalizeMinMax(100, 0, 100);

        // then
        assertThat(result).isEqualTo(100.0);
    }

    @Test
    @DisplayName("Min-Max 정규화 - 중간값")
    void normalizeMinMax_MiddleValue() {
        // when
        double result = NormalizationUtil.normalizeMinMax(50, 0, 100);

        // then
        assertThat(result).isEqualTo(50.0);
    }

    @Test
    @DisplayName("Min-Max 정규화 - 25%")
    void normalizeMinMax_QuarterValue() {
        // when
        double result = NormalizationUtil.normalizeMinMax(25, 0, 100);

        // then
        assertThat(result).isEqualTo(25.0);
    }

    @Test
    @DisplayName("Min-Max 정규화 - 실수 범위")
    void normalizeMinMax_DoubleRange() {
        // when
        double result = NormalizationUtil.normalizeMinMax(1.5, 1.0, 2.0);

        // then
        assertThat(result).isEqualTo(50.0);
    }

    @Test
    @DisplayName("Min-Max 정규화 - 모든 값 동일 시 50 반환")
    void normalizeMinMax_SameValues() {
        // when
        double result = NormalizationUtil.normalizeMinMax(10, 10, 10);

        // then
        assertThat(result).isEqualTo(50.0);
    }

    @Test
    @DisplayName("로그 정규화 - 최소값")
    void normalizeLog_MinValue() {
        // when
        double result = NormalizationUtil.normalizeLog(0, 0, 100);

        // then
        assertThat(result).isEqualTo(0.0);
    }

    @Test
    @DisplayName("로그 정규화 - 큰 편차 완화")
    void normalizeLog_LargeDifference() {
        // when
        double result1 = NormalizationUtil.normalizeLog(10, 1, 1000);
        double result2 = NormalizationUtil.normalizeLog(100, 1, 1000);

        // then
        // 로그 스케일이므로 10배 차이가 정규화 후에는 더 작은 차이
        assertThat(result2 - result1).isLessThan(50.0);
        assertThat(result2).isGreaterThan(result1);
    }

    @Test
    @DisplayName("로그 정규화 - 0값 처리 (log(1+0) = 0)")
    void normalizeLog_ZeroValue() {
        // when
        double result = NormalizationUtil.normalizeLog(0, 0, 10);

        // then
        assertThat(result).isEqualTo(0.0);
    }

    @Test
    @DisplayName("선형 정규화 - 음수 범위를 0~100으로 변환")
    void normalize_NegativeToPositiveRange() {
        // given: -100 ~ 100 범위
        // when
        double result1 = NormalizationUtil.normalize(-100, -100, 100);
        double result2 = NormalizationUtil.normalize(0, -100, 100);
        double result3 = NormalizationUtil.normalize(100, -100, 100);

        // then
        assertThat(result1).isEqualTo(0.0);   // 최소값 → 0
        assertThat(result2).isEqualTo(50.0);  // 중간값 → 50
        assertThat(result3).isEqualTo(100.0); // 최대값 → 100
    }

    @Test
    @DisplayName("선형 정규화 - 카테고리 선호도 변환 (-100, 0, 100 → 0, 50, 100)")
    void normalize_CategoryPreference() {
        // given: 카테고리 선호도 -100(싫어요), 0(보통), 100(좋아요)
        // when
        double disliked = NormalizationUtil.normalize(-100, -100, 100);
        double neutral = NormalizationUtil.normalize(0, -100, 100);
        double liked = NormalizationUtil.normalize(100, -100, 100);

        // then
        assertThat(disliked).isEqualTo(0.0);
        assertThat(neutral).isEqualTo(50.0);
        assertThat(liked).isEqualTo(100.0);
    }
}

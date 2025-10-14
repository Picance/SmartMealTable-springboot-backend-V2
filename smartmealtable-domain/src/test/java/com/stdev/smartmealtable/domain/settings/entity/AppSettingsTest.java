package com.stdev.smartmealtable.domain.settings.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AppSettings 도메인 엔티티 테스트
 */
class AppSettingsTest {

    @Test
    @DisplayName("앱 설정 생성 - 기본값 추적 비활성화")
    void create_Success_DefaultTrackingDisabled() {
        // given
        Long memberId = 1L;

        // when
        AppSettings settings = AppSettings.create(memberId);

        // then
        assertThat(settings).isNotNull();
        assertThat(settings.getMemberId()).isEqualTo(memberId);
        assertThat(settings.isAllowTracking()).isFalse();  // 기본값: 추적 비활성화
    }

    @Test
    @DisplayName("앱 설정 재구성")
    void reconstitute_Success() {
        // given
        Long appSettingsId = 100L;
        Long memberId = 1L;
        boolean allowTracking = true;

        // when
        AppSettings settings = AppSettings.reconstitute(
                appSettingsId,
                memberId,
                allowTracking
        );

        // then
        assertThat(settings).isNotNull();
        assertThat(settings.getAppSettingsId()).isEqualTo(appSettingsId);
        assertThat(settings.getMemberId()).isEqualTo(memberId);
        assertThat(settings.isAllowTracking()).isTrue();
    }

    @Test
    @DisplayName("추적 설정 변경 - true로 활성화")
    void updateTrackingSettings_Success_EnableTracking() {
        // given
        AppSettings settings = AppSettings.create(1L);  // 기본값 false

        // when
        settings.updateTrackingSettings(true);

        // then
        assertThat(settings.isAllowTracking()).isTrue();
    }

    @Test
    @DisplayName("추적 설정 변경 - false로 비활성화")
    void updateTrackingSettings_Success_DisableTracking() {
        // given
        AppSettings settings = AppSettings.reconstitute(1L, 1L, true);  // 초기값 true

        // when
        settings.updateTrackingSettings(false);

        // then
        assertThat(settings.isAllowTracking()).isFalse();
    }

    @Test
    @DisplayName("추적 설정 토글 - 반복 변경")
    void updateTrackingSettings_Success_Toggle() {
        // given
        AppSettings settings = AppSettings.create(1L);

        // when & then
        assertThat(settings.isAllowTracking()).isFalse();

        settings.updateTrackingSettings(true);
        assertThat(settings.isAllowTracking()).isTrue();

        settings.updateTrackingSettings(false);
        assertThat(settings.isAllowTracking()).isFalse();

        settings.updateTrackingSettings(true);
        assertThat(settings.isAllowTracking()).isTrue();
    }
}

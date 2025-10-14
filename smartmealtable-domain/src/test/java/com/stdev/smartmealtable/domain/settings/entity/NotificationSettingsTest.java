package com.stdev.smartmealtable.domain.settings.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * NotificationSettings 도메인 엔티티 테스트
 */
class NotificationSettingsTest {

    @Test
    @DisplayName("알림 설정 생성 - 기본값 모두 활성화")
    void create_Success_DefaultAllEnabled() {
        // given
        Long memberId = 1L;

        // when
        NotificationSettings settings = NotificationSettings.create(memberId);

        // then
        assertThat(settings).isNotNull();
        assertThat(settings.getMemberId()).isEqualTo(memberId);
        assertThat(settings.isPushEnabled()).isTrue();
        assertThat(settings.isStoreNoticeEnabled()).isTrue();
        assertThat(settings.isRecommendationEnabled()).isTrue();
        assertThat(settings.isBudgetAlertEnabled()).isTrue();
        assertThat(settings.isPasswordExpiryAlertEnabled()).isTrue();
    }

    @Test
    @DisplayName("알림 설정 재구성")
    void reconstitute_Success() {
        // given
        Long notificationSettingsId = 100L;
        Long memberId = 1L;

        // when
        NotificationSettings settings = NotificationSettings.reconstitute(
                notificationSettingsId,
                memberId,
                false,
                false,
                true,
                true,
                false
        );

        // then
        assertThat(settings).isNotNull();
        assertThat(settings.getNotificationSettingsId()).isEqualTo(notificationSettingsId);
        assertThat(settings.getMemberId()).isEqualTo(memberId);
        assertThat(settings.isPushEnabled()).isFalse();
        assertThat(settings.isStoreNoticeEnabled()).isFalse();
        assertThat(settings.isRecommendationEnabled()).isTrue();
        assertThat(settings.isBudgetAlertEnabled()).isTrue();
        assertThat(settings.isPasswordExpiryAlertEnabled()).isFalse();
    }

    @Test
    @DisplayName("알림 설정 업데이트 - pushEnabled가 true일 때")
    void updateSettings_Success_PushEnabled() {
        // given
        NotificationSettings settings = NotificationSettings.create(1L);

        // when
        settings.updateSettings(true, false, true, false, true);

        // then
        assertThat(settings.isPushEnabled()).isTrue();
        assertThat(settings.isStoreNoticeEnabled()).isFalse();
        assertThat(settings.isRecommendationEnabled()).isTrue();
        assertThat(settings.isBudgetAlertEnabled()).isFalse();
        assertThat(settings.isPasswordExpiryAlertEnabled()).isTrue();
    }

    @Test
    @DisplayName("알림 설정 업데이트 - pushEnabled가 false면 모든 하위 알림 비활성화 (REQ-PROFILE-302a)")
    void updateSettings_Success_PushDisabled_AllSubNotificationsDisabled() {
        // given
        NotificationSettings settings = NotificationSettings.create(1L);

        // when - pushEnabled를 false로 설정 (하위 알림은 true로 요청해도 무시됨)
        settings.updateSettings(false, true, true, true, true);

        // then - 모든 알림이 비활성화되어야 함
        assertThat(settings.isPushEnabled()).isFalse();
        assertThat(settings.isStoreNoticeEnabled()).isFalse();
        assertThat(settings.isRecommendationEnabled()).isFalse();
        assertThat(settings.isBudgetAlertEnabled()).isFalse();
        assertThat(settings.isPasswordExpiryAlertEnabled()).isFalse();
    }

    @Test
    @DisplayName("푸시 알림 활성화 - 하위 알림 설정 유지")
    void enablePush_Success_SubNotificationsPreserved() {
        // given
        NotificationSettings settings = NotificationSettings.reconstitute(
                1L, 1L, false, false, true, false, true
        );

        // when
        settings.enablePush();

        // then
        assertThat(settings.isPushEnabled()).isTrue();
        // 하위 알림 설정은 이전 상태 유지
        assertThat(settings.isStoreNoticeEnabled()).isFalse();
        assertThat(settings.isRecommendationEnabled()).isTrue();
        assertThat(settings.isBudgetAlertEnabled()).isFalse();
        assertThat(settings.isPasswordExpiryAlertEnabled()).isTrue();
    }

    @Test
    @DisplayName("푸시 알림 비활성화 - 모든 하위 알림 함께 비활성화")
    void disablePush_Success_AllSubNotificationsDisabled() {
        // given
        NotificationSettings settings = NotificationSettings.create(1L);  // 모두 true 상태

        // when
        settings.disablePush();

        // then
        assertThat(settings.isPushEnabled()).isFalse();
        assertThat(settings.isStoreNoticeEnabled()).isFalse();
        assertThat(settings.isRecommendationEnabled()).isFalse();
        assertThat(settings.isBudgetAlertEnabled()).isFalse();
        assertThat(settings.isPasswordExpiryAlertEnabled()).isFalse();
    }

    @Test
    @DisplayName("푸시 알림 재활성화 후 개별 설정 가능")
    void enablePushAgain_Success_IndividualSettingsAllowed() {
        // given
        NotificationSettings settings = NotificationSettings.create(1L);
        settings.disablePush();  // 전체 비활성화

        // when - 푸시 활성화 후 개별 설정
        settings.enablePush();
        settings.updateSettings(true, true, false, true, false);

        // then
        assertThat(settings.isPushEnabled()).isTrue();
        assertThat(settings.isStoreNoticeEnabled()).isTrue();
        assertThat(settings.isRecommendationEnabled()).isFalse();
        assertThat(settings.isBudgetAlertEnabled()).isTrue();
        assertThat(settings.isPasswordExpiryAlertEnabled()).isFalse();
    }
}

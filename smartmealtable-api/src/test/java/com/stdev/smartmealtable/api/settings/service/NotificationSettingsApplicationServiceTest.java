package com.stdev.smartmealtable.api.settings.service;

import com.stdev.smartmealtable.api.settings.dto.NotificationSettingsServiceResponse;
import com.stdev.smartmealtable.api.settings.dto.UpdateNotificationSettingsServiceRequest;
import com.stdev.smartmealtable.domain.settings.entity.NotificationSettings;
import com.stdev.smartmealtable.domain.settings.repository.NotificationSettingsRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * NotificationSettingsApplicationService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class NotificationSettingsApplicationServiceTest {

    @Mock
    private NotificationSettingsRepository notificationSettingsRepository;

    @InjectMocks
    private NotificationSettingsApplicationService notificationSettingsApplicationService;

    @Test
    @DisplayName("알림 설정 조회 성공 - 기존 설정 존재")
    void getNotificationSettings_Success_ExistingSettings() {
        // given
        Long memberId = 1L;
        NotificationSettings existingSettings = NotificationSettings.create(memberId);
        existingSettings.updateSettings(true, true, false, true, false);

        given(notificationSettingsRepository.findByMemberId(memberId))
                .willReturn(Optional.of(existingSettings));

        // when
        NotificationSettingsServiceResponse response = 
                notificationSettingsApplicationService.getNotificationSettings(memberId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.pushEnabled()).isTrue();
        assertThat(response.storeNoticeEnabled()).isTrue();
        assertThat(response.recommendationEnabled()).isFalse();
        assertThat(response.budgetAlertEnabled()).isTrue();
        assertThat(response.passwordExpiryAlertEnabled()).isFalse();

        verify(notificationSettingsRepository).findByMemberId(memberId);
        verify(notificationSettingsRepository, never()).save(any());
    }

    @Test
    @DisplayName("알림 설정 조회 성공 - 기본값 생성")
    void getNotificationSettings_Success_CreateDefault() {
        // given
        Long memberId = 1L;
        NotificationSettings newSettings = NotificationSettings.create(memberId);

        given(notificationSettingsRepository.findByMemberId(memberId))
                .willReturn(Optional.empty());
        given(notificationSettingsRepository.save(any(NotificationSettings.class)))
                .willReturn(newSettings);

        // when
        NotificationSettingsServiceResponse response = 
                notificationSettingsApplicationService.getNotificationSettings(memberId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.pushEnabled()).isTrue();  // 기본값 true
        assertThat(response.storeNoticeEnabled()).isTrue();
        assertThat(response.recommendationEnabled()).isTrue();
        assertThat(response.budgetAlertEnabled()).isTrue();
        assertThat(response.passwordExpiryAlertEnabled()).isTrue();

        verify(notificationSettingsRepository).findByMemberId(memberId);
        verify(notificationSettingsRepository).save(any(NotificationSettings.class));
    }

    @Test
    @DisplayName("알림 설정 변경 성공 - 기존 설정 존재")
    void updateNotificationSettings_Success_ExistingSettings() {
        // given
        Long memberId = 1L;
        NotificationSettings existingSettings = NotificationSettings.create(memberId);
        
        UpdateNotificationSettingsServiceRequest request = 
                new UpdateNotificationSettingsServiceRequest(false, false, false, true, true);

        given(notificationSettingsRepository.findByMemberId(memberId))
                .willReturn(Optional.of(existingSettings));
        given(notificationSettingsRepository.save(any(NotificationSettings.class)))
                .willAnswer(invocation -> invocation.getArgument(0));  // 저장한 객체를 그대로 반환

        // when
        NotificationSettingsServiceResponse response = 
                notificationSettingsApplicationService.updateNotificationSettings(memberId, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.pushEnabled()).isFalse();
        assertThat(response.storeNoticeEnabled()).isFalse();  // pushEnabled가 false면 모두 false
        assertThat(response.recommendationEnabled()).isFalse();  // pushEnabled가 false면 모두 false
        assertThat(response.budgetAlertEnabled()).isFalse();  // pushEnabled가 false면 모두 false
        assertThat(response.passwordExpiryAlertEnabled()).isFalse();  // pushEnabled가 false면 모두 false

        verify(notificationSettingsRepository).findByMemberId(memberId);
        verify(notificationSettingsRepository).save(existingSettings);
    }

    @Test
    @DisplayName("알림 설정 변경 성공 - 신규 생성")
    void updateNotificationSettings_Success_NewSettings() {
        // given
        Long memberId = 1L;
        
        UpdateNotificationSettingsServiceRequest request = 
                new UpdateNotificationSettingsServiceRequest(true, true, true, false, false);

        given(notificationSettingsRepository.findByMemberId(memberId))
                .willReturn(Optional.empty());
        given(notificationSettingsRepository.save(any(NotificationSettings.class)))
                .willAnswer(invocation -> invocation.getArgument(0));  // 저장한 객체를 그대로 반환

        // when
        NotificationSettingsServiceResponse response = 
                notificationSettingsApplicationService.updateNotificationSettings(memberId, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.pushEnabled()).isTrue();
        assertThat(response.storeNoticeEnabled()).isTrue();
        assertThat(response.recommendationEnabled()).isTrue();
        assertThat(response.budgetAlertEnabled()).isFalse();
        assertThat(response.passwordExpiryAlertEnabled()).isFalse();

        verify(notificationSettingsRepository).findByMemberId(memberId);
        verify(notificationSettingsRepository).save(any(NotificationSettings.class));
    }

    @Test
    @DisplayName("알림 설정 변경 성공 - pushEnabled false면 하위 알림 모두 false")
    void updateNotificationSettings_Success_PushDisabled() {
        // given
        Long memberId = 1L;
        NotificationSettings existingSettings = NotificationSettings.create(memberId);
        
        // pushEnabled가 false인 경우 하위 알림도 모두 false로 설정
        UpdateNotificationSettingsServiceRequest request = 
                new UpdateNotificationSettingsServiceRequest(false, false, false, false, false);

        given(notificationSettingsRepository.findByMemberId(memberId))
                .willReturn(Optional.of(existingSettings));
        given(notificationSettingsRepository.save(any(NotificationSettings.class)))
                .willReturn(existingSettings);

        // when
        NotificationSettingsServiceResponse response = 
                notificationSettingsApplicationService.updateNotificationSettings(memberId, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.pushEnabled()).isFalse();
        assertThat(response.storeNoticeEnabled()).isFalse();
        assertThat(response.recommendationEnabled()).isFalse();
        assertThat(response.budgetAlertEnabled()).isFalse();
        assertThat(response.passwordExpiryAlertEnabled()).isFalse();

        verify(notificationSettingsRepository).findByMemberId(memberId);
        verify(notificationSettingsRepository).save(existingSettings);
    }
}

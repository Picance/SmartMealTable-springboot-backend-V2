package com.stdev.smartmealtable.api.settings.service;

import com.stdev.smartmealtable.api.settings.dto.AppSettingsServiceResponse;
import com.stdev.smartmealtable.api.settings.dto.TrackingSettingsServiceResponse;
import com.stdev.smartmealtable.api.settings.dto.UpdateTrackingSettingsServiceRequest;
import com.stdev.smartmealtable.domain.settings.entity.AppSettings;
import com.stdev.smartmealtable.domain.settings.repository.AppSettingsRepository;
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
import static org.mockito.Mockito.verify;

/**
 * AppSettingsApplicationService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class AppSettingsApplicationServiceTest {

    @Mock
    private AppSettingsRepository appSettingsRepository;

    @InjectMocks
    private AppSettingsApplicationService appSettingsApplicationService;

    @Test
    @DisplayName("앱 설정 정보 조회 성공")
    void getAppSettings_Success() {
        // when
        AppSettingsServiceResponse response = appSettingsApplicationService.getAppSettings();

        // then
        assertThat(response).isNotNull();
        assertThat(response.privacyPolicyUrl()).isEqualTo("https://smartmealtable.com/policies/privacy");
        assertThat(response.termsOfServiceUrl()).isEqualTo("https://smartmealtable.com/policies/terms");
        assertThat(response.contactEmail()).isEqualTo("support@smartmealtable.com");
        assertThat(response.appVersion()).isEqualTo("1.0.0");
        assertThat(response.minSupportedVersion()).isEqualTo("1.0.0");
    }

    @Test
    @DisplayName("사용자 추적 설정 변경 성공 - 기존 설정 존재")
    void updateTrackingSettings_Success_ExistingSettings() {
        // given
        Long memberId = 1L;
        AppSettings existingSettings = AppSettings.create(memberId);
        existingSettings.updateTrackingSettings(false);  // 초기값 false로 설정
        
        UpdateTrackingSettingsServiceRequest request = 
                new UpdateTrackingSettingsServiceRequest(true);

        given(appSettingsRepository.findByMemberId(memberId))
                .willReturn(Optional.of(existingSettings));
        given(appSettingsRepository.save(any(AppSettings.class)))
                .willReturn(existingSettings);

        // when
        TrackingSettingsServiceResponse response = 
                appSettingsApplicationService.updateTrackingSettings(memberId, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.allowTracking()).isTrue();
        assertThat(response.updatedAt()).isNotNull();

        verify(appSettingsRepository).findByMemberId(memberId);
        verify(appSettingsRepository).save(existingSettings);
    }

    @Test
    @DisplayName("사용자 추적 설정 변경 성공 - 신규 생성")
    void updateTrackingSettings_Success_NewSettings() {
        // given
        Long memberId = 1L;
        AppSettings newSettings = AppSettings.create(memberId);
        
        UpdateTrackingSettingsServiceRequest request = 
                new UpdateTrackingSettingsServiceRequest(false);

        given(appSettingsRepository.findByMemberId(memberId))
                .willReturn(Optional.empty());
        given(appSettingsRepository.save(any(AppSettings.class)))
                .willReturn(newSettings);

        // when
        TrackingSettingsServiceResponse response = 
                appSettingsApplicationService.updateTrackingSettings(memberId, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.allowTracking()).isFalse();
        assertThat(response.updatedAt()).isNotNull();

        verify(appSettingsRepository).findByMemberId(memberId);
        verify(appSettingsRepository).save(any(AppSettings.class));
    }

    @Test
    @DisplayName("사용자 추적 설정 변경 성공 - true로 변경")
    void updateTrackingSettings_Success_EnableTracking() {
        // given
        Long memberId = 1L;
        AppSettings existingSettings = AppSettings.create(memberId);
        
        UpdateTrackingSettingsServiceRequest request = 
                new UpdateTrackingSettingsServiceRequest(true);

        given(appSettingsRepository.findByMemberId(memberId))
                .willReturn(Optional.of(existingSettings));
        given(appSettingsRepository.save(any(AppSettings.class)))
                .willReturn(existingSettings);

        // when
        TrackingSettingsServiceResponse response = 
                appSettingsApplicationService.updateTrackingSettings(memberId, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.allowTracking()).isTrue();

        verify(appSettingsRepository).findByMemberId(memberId);
        verify(appSettingsRepository).save(existingSettings);
    }

    @Test
    @DisplayName("사용자 추적 설정 변경 성공 - false로 변경")
    void updateTrackingSettings_Success_DisableTracking() {
        // given
        Long memberId = 1L;
        AppSettings existingSettings = AppSettings.create(memberId);
        existingSettings.updateTrackingSettings(true);  // 초기값 true로 설정
        
        UpdateTrackingSettingsServiceRequest request = 
                new UpdateTrackingSettingsServiceRequest(false);

        given(appSettingsRepository.findByMemberId(memberId))
                .willReturn(Optional.of(existingSettings));
        given(appSettingsRepository.save(any(AppSettings.class)))
                .willReturn(existingSettings);

        // when
        TrackingSettingsServiceResponse response = 
                appSettingsApplicationService.updateTrackingSettings(memberId, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.allowTracking()).isFalse();

        verify(appSettingsRepository).findByMemberId(memberId);
        verify(appSettingsRepository).save(existingSettings);
    }
}

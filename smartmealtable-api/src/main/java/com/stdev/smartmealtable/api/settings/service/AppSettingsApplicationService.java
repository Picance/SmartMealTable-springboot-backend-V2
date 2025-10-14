package com.stdev.smartmealtable.api.settings.service;

import com.stdev.smartmealtable.api.settings.dto.AppSettingsServiceResponse;
import com.stdev.smartmealtable.api.settings.dto.TrackingSettingsServiceResponse;
import com.stdev.smartmealtable.api.settings.dto.UpdateTrackingSettingsServiceRequest;
import com.stdev.smartmealtable.domain.settings.entity.AppSettings;
import com.stdev.smartmealtable.domain.settings.repository.AppSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 앱 설정 Application Service
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AppSettingsApplicationService {

    private final AppSettingsRepository appSettingsRepository;

    /**
     * 앱 설정 정보 조회
     * 
     * @return 앱 설정 정보 (정적 정보)
     */
    public AppSettingsServiceResponse getAppSettings() {
        log.info("앱 설정 정보 조회");
        return AppSettingsServiceResponse.createDefault();
    }

    /**
     * 사용자 추적 설정 변경
     * 
     * @param memberId 회원 ID
     * @param request 변경 요청
     * @return 변경된 추적 설정
     */
    @Transactional
    public TrackingSettingsServiceResponse updateTrackingSettings(
            Long memberId,
            UpdateTrackingSettingsServiceRequest request
    ) {
        log.info("사용자 추적 설정 변경 요청 - memberId: {}, allowTracking: {}", 
                memberId, request.allowTracking());
        
        AppSettings settings = appSettingsRepository.findByMemberId(memberId)
                .orElseGet(() -> AppSettings.create(memberId));
        
        settings.updateTrackingSettings(request.allowTracking());
        
        AppSettings saved = appSettingsRepository.save(settings);
        
        log.info("사용자 추적 설정 변경 완료 - memberId: {}, allowTracking: {}", 
                memberId, saved.isAllowTracking());
        
        return new TrackingSettingsServiceResponse(
                saved.isAllowTracking(),
                LocalDateTime.now()
        );
    }
}

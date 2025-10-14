package com.stdev.smartmealtable.api.settings.service;

import com.stdev.smartmealtable.api.settings.dto.NotificationSettingsServiceResponse;
import com.stdev.smartmealtable.api.settings.dto.UpdateNotificationSettingsServiceRequest;
import com.stdev.smartmealtable.domain.settings.entity.NotificationSettings;
import com.stdev.smartmealtable.domain.settings.repository.NotificationSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 알림 설정 Application Service
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class NotificationSettingsApplicationService {

    private final NotificationSettingsRepository notificationSettingsRepository;

    /**
     * 알림 설정 조회
     * 
     * @param memberId 회원 ID
     * @return 알림 설정
     */
    public NotificationSettingsServiceResponse getNotificationSettings(Long memberId) {
        log.info("알림 설정 조회 요청 - memberId: {}", memberId);
        
        NotificationSettings settings = notificationSettingsRepository.findByMemberId(memberId)
                .orElseGet(() -> {
                    log.info("알림 설정이 없어 기본값으로 생성 - memberId: {}", memberId);
                    NotificationSettings newSettings = NotificationSettings.create(memberId);
                    return notificationSettingsRepository.save(newSettings);
                });
        
        return new NotificationSettingsServiceResponse(
                settings.isPushEnabled(),
                settings.isStoreNoticeEnabled(),
                settings.isRecommendationEnabled(),
                settings.isBudgetAlertEnabled(),
                settings.isPasswordExpiryAlertEnabled()
        );
    }

    /**
     * 알림 설정 변경
     * 
     * @param memberId 회원 ID
     * @param request 변경 요청
     * @return 변경된 알림 설정
     */
    @Transactional
    public NotificationSettingsServiceResponse updateNotificationSettings(
            Long memberId,
            UpdateNotificationSettingsServiceRequest request
    ) {
        log.info("알림 설정 변경 요청 - memberId: {}, pushEnabled: {}", 
                memberId, request.pushEnabled());
        
        NotificationSettings settings = notificationSettingsRepository.findByMemberId(memberId)
                .orElseGet(() -> NotificationSettings.create(memberId));
        
        settings.updateSettings(
                request.pushEnabled(),
                request.storeNoticeEnabled(),
                request.recommendationEnabled(),
                request.budgetAlertEnabled(),
                request.passwordExpiryAlertEnabled()
        );
        
        NotificationSettings saved = notificationSettingsRepository.save(settings);
        
        log.info("알림 설정 변경 완료 - memberId: {}, pushEnabled: {}", 
                memberId, saved.isPushEnabled());
        
        return new NotificationSettingsServiceResponse(
                saved.isPushEnabled(),
                saved.isStoreNoticeEnabled(),
                saved.isRecommendationEnabled(),
                saved.isBudgetAlertEnabled(),
                saved.isPasswordExpiryAlertEnabled()
        );
    }
}

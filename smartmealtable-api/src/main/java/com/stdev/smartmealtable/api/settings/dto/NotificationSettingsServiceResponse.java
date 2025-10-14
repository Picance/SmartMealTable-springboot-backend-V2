package com.stdev.smartmealtable.api.settings.dto;

/**
 * 알림 설정 응답 DTO
 */
public record NotificationSettingsServiceResponse(
        boolean pushEnabled,
        boolean storeNoticeEnabled,
        boolean recommendationEnabled,
        boolean budgetAlertEnabled,
        boolean passwordExpiryAlertEnabled
) {
}

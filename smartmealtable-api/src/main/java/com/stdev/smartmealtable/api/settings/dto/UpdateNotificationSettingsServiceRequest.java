package com.stdev.smartmealtable.api.settings.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 알림 설정 변경 요청 DTO
 */
public record UpdateNotificationSettingsServiceRequest(
        @NotNull(message = "전체 푸시 알림 활성화 여부는 필수입니다.")
        Boolean pushEnabled,
        
        @NotNull(message = "가게 공지 알림 활성화 여부는 필수입니다.")
        Boolean storeNoticeEnabled,
        
        @NotNull(message = "음식점 추천 알림 활성화 여부는 필수입니다.")
        Boolean recommendationEnabled,
        
        @NotNull(message = "예산 알림 활성화 여부는 필수입니다.")
        Boolean budgetAlertEnabled,
        
        @NotNull(message = "비밀번호 만료 알림 활성화 여부는 필수입니다.")
        Boolean passwordExpiryAlertEnabled
) {
}

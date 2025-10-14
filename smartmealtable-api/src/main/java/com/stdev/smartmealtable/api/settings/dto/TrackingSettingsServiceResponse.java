package com.stdev.smartmealtable.api.settings.dto;

import java.time.LocalDateTime;

/**
 * 사용자 추적 설정 응답 DTO
 */
public record TrackingSettingsServiceResponse(
        boolean allowTracking,
        LocalDateTime updatedAt
) {
}

package com.stdev.smartmealtable.api.settings.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 사용자 추적 설정 변경 요청 DTO
 */
public record UpdateTrackingSettingsServiceRequest(
        @NotNull(message = "사용자 추적 허용 여부는 필수입니다.")
        Boolean allowTracking
) {
}

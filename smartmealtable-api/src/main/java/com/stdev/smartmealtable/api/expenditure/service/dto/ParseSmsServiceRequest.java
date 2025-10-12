package com.stdev.smartmealtable.api.expenditure.service.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * SMS 파싱 요청 DTO
 */
public record ParseSmsServiceRequest(
        @NotBlank(message = "SMS 메시지는 필수입니다.")
        String smsMessage
) {
}

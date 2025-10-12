package com.stdev.smartmealtable.api.expenditure.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * SMS 파싱 요청 Request DTO
 */
public record ParseSmsRequest(
        @NotBlank(message = "SMS 메시지는 필수입니다.")
        String smsMessage
) {
}

package com.stdev.smartmealtable.api.expenditure.service.dto;

import java.time.LocalDateTime;

/**
 * SMS 파싱 응답 DTO
 */
public record ParseSmsServiceResponse(
        String vendor,
        LocalDateTime transactionDateTime,
        Long amount,
        String storeName
) {
}

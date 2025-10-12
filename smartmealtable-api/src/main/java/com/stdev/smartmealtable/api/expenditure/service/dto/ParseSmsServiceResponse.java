package com.stdev.smartmealtable.api.expenditure.service.dto;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * SMS 파싱 응답 DTO
 * API Spec 기준: storeName, amount, date, time, isParsed
 */
public record ParseSmsServiceResponse(
        String storeName,
        Long amount,
        LocalDate date,
        LocalTime time,
        boolean isParsed
) {
}

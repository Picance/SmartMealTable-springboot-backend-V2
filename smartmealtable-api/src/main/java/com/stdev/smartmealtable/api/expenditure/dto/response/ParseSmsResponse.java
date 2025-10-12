package com.stdev.smartmealtable.api.expenditure.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.stdev.smartmealtable.api.expenditure.service.dto.ParseSmsServiceResponse;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * SMS 파싱 응답 Response DTO
 * API Spec: storeName, amount, date, time, isParsed
 */
public record ParseSmsResponse(
        String storeName,
        Long amount,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,
        @JsonFormat(pattern = "HH:mm:ss")
        LocalTime time,
        boolean isParsed
) {
    public static ParseSmsResponse from(ParseSmsServiceResponse serviceResponse) {
        return new ParseSmsResponse(
                serviceResponse.storeName(),
                serviceResponse.amount(),
                serviceResponse.date(),
                serviceResponse.time(),
                serviceResponse.isParsed()
        );
    }
}

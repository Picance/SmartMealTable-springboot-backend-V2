package com.stdev.smartmealtable.api.expenditure.dto.response;

import com.stdev.smartmealtable.api.expenditure.service.dto.ParseSmsServiceResponse;

import java.time.LocalDateTime;

/**
 * SMS 파싱 응답 Response DTO
 */
public record ParseSmsResponse(
        String vendor,
        LocalDateTime transactionDateTime,
        Long amount,
        String storeName
) {
    public static ParseSmsResponse from(ParseSmsServiceResponse serviceResponse) {
        return new ParseSmsResponse(
                serviceResponse.vendor(),
                serviceResponse.transactionDateTime(),
                serviceResponse.amount(),
                serviceResponse.storeName()
        );
    }
}

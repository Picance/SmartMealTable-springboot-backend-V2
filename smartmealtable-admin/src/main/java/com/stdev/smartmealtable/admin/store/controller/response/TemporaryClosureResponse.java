package com.stdev.smartmealtable.admin.store.controller.response;

import com.stdev.smartmealtable.admin.store.service.dto.TemporaryClosureServiceResponse;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 임시 휴무 정보 Response DTO (Controller)
 */
public record TemporaryClosureResponse(
        Long storeTemporaryClosureId,
        Long storeId,
        LocalDate closureDate,
        LocalTime startTime,
        LocalTime endTime,
        String reason
) {
    public static TemporaryClosureResponse from(TemporaryClosureServiceResponse serviceResponse) {
        return new TemporaryClosureResponse(
                serviceResponse.storeTemporaryClosureId(),
                serviceResponse.storeId(),
                serviceResponse.closureDate(),
                serviceResponse.startTime(),
                serviceResponse.endTime(),
                serviceResponse.reason()
        );
    }
}

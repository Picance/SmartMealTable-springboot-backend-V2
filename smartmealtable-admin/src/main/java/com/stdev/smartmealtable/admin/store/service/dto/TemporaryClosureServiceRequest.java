package com.stdev.smartmealtable.admin.store.service.dto;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 임시 휴무 등록 Service Request
 */
public record TemporaryClosureServiceRequest(
        LocalDate closureDate,
        LocalTime startTime,
        LocalTime endTime,
        String reason
) {
    public static TemporaryClosureServiceRequest of(
            LocalDate closureDate,
            LocalTime startTime,
            LocalTime endTime,
            String reason
    ) {
        return new TemporaryClosureServiceRequest(
                closureDate,
                startTime,
                endTime,
                reason
        );
    }
}

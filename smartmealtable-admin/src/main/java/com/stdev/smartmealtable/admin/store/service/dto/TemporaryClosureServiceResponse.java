package com.stdev.smartmealtable.admin.store.service.dto;

import com.stdev.smartmealtable.domain.store.StoreTemporaryClosure;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 임시 휴무 정보 Service Response
 */
public record TemporaryClosureServiceResponse(
        Long storeTemporaryClosureId,
        Long storeId,
        LocalDate closureDate,
        LocalTime startTime,
        LocalTime endTime,
        String reason
) {
    public static TemporaryClosureServiceResponse from(StoreTemporaryClosure closure) {
        return new TemporaryClosureServiceResponse(
                closure.storeTemporaryClosureId(),
                closure.storeId(),
                closure.closureDate(),
                closure.startTime(),
                closure.endTime(),
                closure.reason()
        );
    }
}

package com.stdev.smartmealtable.admin.store.controller.response;

import com.stdev.smartmealtable.admin.store.service.dto.OpeningHourServiceResponse;

import java.time.DayOfWeek;

/**
 * 영업시간 정보 Response DTO (Controller)
 */
public record OpeningHourResponse(
        Long storeOpeningHourId,
        Long storeId,
        DayOfWeek dayOfWeek,
        String openTime,
        String closeTime,
        String breakStartTime,
        String breakEndTime,
        boolean isHoliday
) {
    public static OpeningHourResponse from(OpeningHourServiceResponse serviceResponse) {
        return new OpeningHourResponse(
                serviceResponse.storeOpeningHourId(),
                serviceResponse.storeId(),
                serviceResponse.dayOfWeek(),
                serviceResponse.openTime(),
                serviceResponse.closeTime(),
                serviceResponse.breakStartTime(),
                serviceResponse.breakEndTime(),
                serviceResponse.isHoliday()
        );
    }
}

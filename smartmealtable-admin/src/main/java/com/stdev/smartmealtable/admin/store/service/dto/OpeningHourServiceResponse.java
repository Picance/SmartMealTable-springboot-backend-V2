package com.stdev.smartmealtable.admin.store.service.dto;

import com.stdev.smartmealtable.domain.store.StoreOpeningHour;

import java.time.DayOfWeek;

/**
 * 영업시간 정보 Service Response
 */
public record OpeningHourServiceResponse(
        Long storeOpeningHourId,
        Long storeId,
        DayOfWeek dayOfWeek,
        String openTime,
        String closeTime,
        String breakStartTime,
        String breakEndTime,
        boolean isHoliday
) {
    public static OpeningHourServiceResponse from(StoreOpeningHour openingHour) {
        return new OpeningHourServiceResponse(
                openingHour.storeOpeningHourId(),
                openingHour.storeId(),
                openingHour.dayOfWeek(),
                openingHour.openTime(),
                openingHour.closeTime(),
                openingHour.breakStartTime(),
                openingHour.breakEndTime(),
                openingHour.isHoliday()
        );
    }
}

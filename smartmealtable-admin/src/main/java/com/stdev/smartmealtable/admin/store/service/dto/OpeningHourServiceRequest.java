package com.stdev.smartmealtable.admin.store.service.dto;

import java.time.DayOfWeek;

/**
 * 영업시간 추가/수정 Service Request
 */
public record OpeningHourServiceRequest(
        DayOfWeek dayOfWeek,
        String openTime,
        String closeTime,
        String breakStartTime,
        String breakEndTime,
        boolean isHoliday
) {
    public static OpeningHourServiceRequest of(
            DayOfWeek dayOfWeek,
            String openTime,
            String closeTime,
            String breakStartTime,
            String breakEndTime,
            boolean isHoliday
    ) {
        return new OpeningHourServiceRequest(
                dayOfWeek,
                openTime,
                closeTime,
                breakStartTime,
                breakEndTime,
                isHoliday
        );
    }
}

package com.stdev.smartmealtable.domain.store;

import java.time.DayOfWeek;

/**
 * 영업 시간 정보를 담는 값 객체
 */
public record StoreOpeningHour(
        Long storeOpeningHourId,
        Long storeId,
        DayOfWeek dayOfWeek,
        String openTime,
        String closeTime,
        String breakStartTime,
        String breakEndTime,
        boolean isHoliday
) {
    /**
     * 휴무일인지 확인
     */
    public boolean isHoliday() {
        return isHoliday;
    }
    
    /**
     * 정규 영업일인지 확인
     */
    public boolean isRegularDay() {
        return !isHoliday;
    }
}

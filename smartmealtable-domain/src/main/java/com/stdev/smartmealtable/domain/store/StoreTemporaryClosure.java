package com.stdev.smartmealtable.domain.store;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 가게 임시 휴무 정보를 담는 값 객체
 */
public record StoreTemporaryClosure(
        Long storeTemporaryClosureId,
        Long storeId,
        LocalDate closureDate,
        LocalTime startTime,
        LocalTime endTime,
        String reason
) {
    /**
     * 특정 날짜가 임시 휴무 기간에 해당하는지 확인
     */
    public boolean isClosedOn(LocalDate date) {
        return closureDate.equals(date);
    }
}

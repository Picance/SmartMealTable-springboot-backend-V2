package com.stdev.smartmealtable.domain.expenditure.sms;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * SMS 파싱 결과를 담는 Value Object
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ParsedSmsResult {

    private final String storeName;
    private final Long amount;
    private final LocalDate date;
    private final LocalTime time;
    private final boolean isParsed;

    public static ParsedSmsResult success(String storeName, Long amount, LocalDate date, LocalTime time) {
        return new ParsedSmsResult(storeName, amount, date, time, true);
    }

    public static ParsedSmsResult failure() {
        return new ParsedSmsResult(null, null, null, null, false);
    }
}

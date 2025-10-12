package com.stdev.smartmealtable.client.external.sms;

import java.time.LocalDateTime;

/**
 * SMS 파싱 결과 객체
 */
public record SmsParsedResult(
        String vendor,
        LocalDateTime transactionDateTime,
        Long amount,
        String storeName
) {
    public SmsParsedResult {
        if (transactionDateTime == null) {
            throw new IllegalArgumentException("거래 일시는 필수입니다.");
        }
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("금액은 0보다 커야 합니다.");
        }
        if (storeName == null || storeName.isBlank()) {
            throw new IllegalArgumentException("가게명은 필수입니다.");
        }
    }
}

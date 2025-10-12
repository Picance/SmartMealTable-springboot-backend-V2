package com.stdev.smartmealtable.domain.expenditure.sms;

/**
 * SMS 파서 인터페이스
 */
public interface SmsParser {

    /**
     * 해당 파서가 주어진 SMS 메시지를 처리할 수 있는지 확인
     */
    boolean canParse(String smsContent);

    /**
     * SMS 메시지를 파싱하여 결과 반환
     */
    ParsedSmsResult parse(String smsContent);
}

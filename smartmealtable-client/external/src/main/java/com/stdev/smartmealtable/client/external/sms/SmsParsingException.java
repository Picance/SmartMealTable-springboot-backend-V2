package com.stdev.smartmealtable.client.external.sms;

/**
 * SMS 파싱 실패 시 발생하는 예외
 */
public class SmsParsingException extends RuntimeException {
    
    public SmsParsingException(String message) {
        super(message);
    }
    
    public SmsParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}

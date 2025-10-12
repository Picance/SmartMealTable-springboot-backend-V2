package com.stdev.smartmealtable.client.external.sms;

/**
 * SMS 파싱 서비스 인터페이스
 */
public interface SmsParsingService {
    
    /**
     * 카드 결제 승인 SMS를 파싱하여 지출 정보를 추출합니다.
     * 
     * @param smsMessage 카드 결제 승인 SMS 원문
     * @return 파싱된 결과 (가게명, 금액, 거래 일시)
     * @throws SmsParsingException 파싱 실패 시
     */
    SmsParsedResult parseSms(String smsMessage);
}

package com.stdev.smartmealtable.api.expenditure.service;

import com.stdev.smartmealtable.api.expenditure.service.dto.ParseSmsServiceRequest;
import com.stdev.smartmealtable.api.expenditure.service.dto.ParseSmsServiceResponse;
import com.stdev.smartmealtable.client.external.sms.SmsParsedResult;
import com.stdev.smartmealtable.client.external.sms.SmsParsingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * SMS 파싱 Application Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ParseSmsService {
    
    private final SmsParsingService smsParsingService;
    
    /**
     * 카드 결제 승인 SMS를 파싱하여 지출 정보를 추출합니다.
     */
    public ParseSmsServiceResponse parseSms(ParseSmsServiceRequest request) {
        log.info("SMS 파싱 요청");
        
        SmsParsedResult parsedResult = smsParsingService.parseSms(request.smsMessage());
        
        return new ParseSmsServiceResponse(
                parsedResult.vendor(),
                parsedResult.transactionDateTime(),
                parsedResult.amount(),
                parsedResult.storeName()
        );
    }
}

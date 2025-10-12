package com.stdev.smartmealtable.api.expenditure.service;

import com.stdev.smartmealtable.api.expenditure.service.dto.ParseSmsServiceRequest;
import com.stdev.smartmealtable.api.expenditure.service.dto.ParseSmsServiceResponse;
import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.domain.expenditure.sms.ParsedSmsResult;
import com.stdev.smartmealtable.domain.expenditure.sms.SmsParsingDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * SMS 파싱 Application Service
 */
@Slf4j
@Service
public class ParseSmsService {
    
    private final SmsParsingDomainService smsParsingDomainService;
    
    public ParseSmsService() {
        this.smsParsingDomainService = new SmsParsingDomainService();
    }
    
    /**
     * 카드 결제 승인 SMS를 파싱하여 지출 정보를 추출합니다.
     */
    public ParseSmsServiceResponse parseSms(ParseSmsServiceRequest request) {
        log.info("SMS 파싱 요청: {}", request.smsMessage());
        
        ParsedSmsResult parsedResult = smsParsingDomainService.parse(request.smsMessage());
        
        if (!parsedResult.isParsed()) {
            log.warn("SMS 파싱 실패 - 지원하지 않는 형식: {}", request.smsMessage());
            throw new BusinessException(ErrorType.SMS_PARSING_FAILED);
        }
        
        return new ParseSmsServiceResponse(
                parsedResult.getStoreName(),
                parsedResult.getAmount(),
                parsedResult.getDate(),
                parsedResult.getTime(),
                parsedResult.isParsed()
        );
    }
}

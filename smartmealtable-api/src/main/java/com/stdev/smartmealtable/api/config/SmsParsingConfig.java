package com.stdev.smartmealtable.api.config;

import com.stdev.smartmealtable.domain.expenditure.sms.SmsParsingDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SMS 파싱 도메인 서비스 빈 등록
 */
@Configuration
public class SmsParsingConfig {

    @Bean
    public SmsParsingDomainService smsParsingDomainService() {
        return new SmsParsingDomainService();
    }
}

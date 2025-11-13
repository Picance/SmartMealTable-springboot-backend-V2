package com.stdev.smartmealtable.api.config;

import com.stdev.smartmealtable.client.external.sms.SmsParsingService;
import com.stdev.smartmealtable.domain.expenditure.sms.SmsParsingDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SMS 파싱 도메인 서비스 및 기본 구현 빈 등록
 */
@Configuration
@Slf4j
public class SmsParsingConfig {

    @Bean
    public SmsParsingDomainService smsParsingDomainService() {
        return new SmsParsingDomainService();
    }

    /**
     * Spring AI 기반 SmsParsingService가 등록되지 않았을 때 동작하는
     * 안전한 기본 구현. AI 기능이 비활성화된 환경에서는 null을 반환하여
     * 도메인 파서의 실패 처리 로직을 그대로 활용한다.
     */
    @Bean
    @ConditionalOnMissingBean(SmsParsingService.class)
    public SmsParsingService fallbackSmsParsingService() {
        return smsMessage -> {
            log.warn("SmsParsingService 빈이 구성되지 않아 AI 기반 파싱을 건너뜁니다. message={}", smsMessage);
            return null;
        };
    }
}

package com.stdev.smartmealtable.client.external.sms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SMS 파싱 서비스 자동 구성
 * <p>
 * Vertex AI 등 외부 파싱 서비스가 등록되지 않은 경우,
 * 안전한 No-Op 구현을 제공하여 애플리케이션이 기동될 수 있도록 합니다.
 */
@Configuration
@Slf4j
public class SmsParsingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(SmsParsingService.class)
    public SmsParsingService smsParsingFallback() {
        return smsMessage -> {
            log.warn("SmsParsingService 구현이 없어 기본 Fallback을 사용합니다. message={}", smsMessage);
            return null;
        };
    }
}

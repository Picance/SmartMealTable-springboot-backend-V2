package com.stdev.smartmealtable.api.config;

import com.stdev.smartmealtable.client.external.sms.GeminiSmsParsingService;
import com.stdev.smartmealtable.client.external.sms.SmsParsingService;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.StreamingChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Flux;

/**
 * 테스트 환경용 ChatModel Mock Configuration
 * 
 * 테스트에서 Spring AI ChatModel이 필요하지만 실제 AI 서비스는 호출하지 않도록
 * Mock ChatModel 빈을 제공합니다.
 */
@TestConfiguration
public class MockChatModelConfig {
    
    /**
     * 테스트용 Mock ChatModel
     *
     * @Primary 어노테이션으로 실제 ChatModel보다 우선순위 높게 설정
     */
    @Bean
    @Primary
    public ChatModel mockChatModel() {
        return new MockChatModel();
    }

    /**
     * 테스트용 Mock GeminiSmsParsingService
     * SMS 파싱 서비스 테스트를 위한 Mock 구현
     */
    @Bean
    public SmsParsingService mockGeminiSmsParsingService() {
        return new SmsParsingService() {
            @Override
            public com.stdev.smartmealtable.client.external.sms.SmsParsedResult parseSms(String smsMessage) {
                return null;  // 테스트에서는 null 반환 (ParseSmsService에서 처리)
            }
        };
    }

    /**
     * Mock ChatModel 구현체
     * 테스트에서는 실제 AI 호출이 필요없으므로 빈 응답 반환
     */
    private static class MockChatModel implements ChatModel, StreamingChatModel {
        
        @Override
        public ChatResponse call(Prompt prompt) {
            // 테스트에서는 사용되지 않음 (ParseSmsService는 ChatClient를 사용하지 않음)
            return null;
        }
        
        @Override
        public Flux<ChatResponse> stream(Prompt prompt) {
            // 테스트에서는 사용되지 않음
            return Flux.empty();
        }
    }
}

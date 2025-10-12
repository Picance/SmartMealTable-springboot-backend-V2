package com.stdev.smartmealtable.api.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI 설정
 * Gemini를 활용한 SMS 파싱 등 AI 기능을 위한 ChatClient 빈을 제공합니다.
 */
@Configuration
public class SpringAiConfig {

    /**
     * ChatClient.Builder 빈 등록
     * Spring AI의 ChatModel을 자동 주입받아 ChatClient.Builder를 생성합니다.
     * application.yml의 spring.ai.vertex.ai.gemini 설정을 사용합니다.
     *
     * @param chatModel Spring Boot가 자동으로 생성한 Vertex AI Gemini ChatModel
     * @return ChatClient.Builder 인스턴스
     */
    @Bean
    public ChatClient.Builder chatClientBuilder(ChatModel chatModel) {
        return ChatClient.builder(chatModel);
    }
}

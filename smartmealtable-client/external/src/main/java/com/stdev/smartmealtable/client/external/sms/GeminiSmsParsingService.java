package com.stdev.smartmealtable.client.external.sms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Spring AI - Gemini를 활용한 SMS 파싱 서비스
 * KB국민, NH농협, 신한 등 다양한 금융기관의 카드 결제 승인 문자를 파싱합니다.
 * 
 * ChatClient.Builder 빈이 존재할 때만 활성화됩니다.
 * 테스트 환경에서는 Vertex AI 설정이 없으므로 자동으로 비활성화됩니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnBean(ChatClient.Builder.class)
public class GeminiSmsParsingService implements SmsParsingService {
    
    private final ChatClient.Builder chatClientBuilder;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    @Override
    public SmsParsedResult parseSms(String smsMessage) {
        if (smsMessage == null || smsMessage.isBlank()) {
            throw new SmsParsingException("SMS 메시지가 비어있습니다.");
        }
        
        log.debug("SMS 파싱 시작: {}", smsMessage);
        
        try {
            ChatClient chatClient = chatClientBuilder.build();
            
            String prompt = createPrompt(smsMessage);
            String jsonResponse = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
            
            log.debug("Gemini 응답: {}", jsonResponse);
            
            return parseJsonResponse(jsonResponse);
            
        } catch (Exception e) {
            log.error("SMS 파싱 실패: {}", e.getMessage(), e);
            throw new SmsParsingException("SMS 파싱에 실패했습니다: " + e.getMessage(), e);
        }
    }
    
    private String createPrompt(String smsMessage) {
        return String.format("""
                너는 대한민국의 신용카드 승인 문자(SMS)를 파싱해서 JSON 형태로 반환하는 전문가야.
                반드시 아래 형식의 JSON 만 출력해. 설명 문구나 코드 블록 표시(```)는 절대 포함하면 안 돼.
                
                {
                  "vendor": "<카드사 영문 약어, 예: KB, NH, SH, UNKNOWN>",
                  "dateTime": "<ISO-8601 형식 yyyy-MM-dd'T'HH:mm:ss>",
                  "amount": <숫자형 원화 금액>,
                  "storeName": "<가맹점명>"
                }
                
                주의사항:
                1. 금액에서 쉼표(,)나 '원' 같은 문자는 제거하고 숫자만 출력
                2. 날짜와 시간은 ISO-8601 형식으로 변환 (년도가 없으면 현재 년도 사용)
                3. 가맹점명은 정확히 추출 (불필요한 공백 제거)
                4. 카드사 이름이 명확하지 않으면 UNKNOWN으로 설정
                
                다음은 파싱 대상 SMS 원문이다:
                %s
                """, smsMessage);
    }
    
    private SmsParsedResult parseJsonResponse(String jsonResponse) {
        try {
            // JSON 응답에서 코드 블록 표시 제거 (혹시 모를 경우 대비)
            String cleanedJson = jsonResponse
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();
            
            JsonNode root = OBJECT_MAPPER.readTree(cleanedJson);
            
            String vendor = root.path("vendor").asText("UNKNOWN");
            String dateTimeStr = root.path("dateTime").asText();
            Long amount = root.path("amount").asLong();
            String storeName = root.path("storeName").asText();
            
            if (dateTimeStr.isBlank()) {
                throw new SmsParsingException("거래 일시를 파싱할 수 없습니다.");
            }
            
            LocalDateTime transactionDateTime = LocalDateTime.parse(dateTimeStr);
            
            return new SmsParsedResult(vendor, transactionDateTime, amount, storeName);
            
        } catch (Exception e) {
            throw new SmsParsingException("JSON 파싱 실패: " + e.getMessage(), e);
        }
    }
}

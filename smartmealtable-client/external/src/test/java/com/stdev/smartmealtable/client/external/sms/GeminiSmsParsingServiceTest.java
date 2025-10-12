package com.stdev.smartmealtable.client.external.sms;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * GeminiSmsParsingService 단위 테스트
 * 
 * Mockist 스타일로 ChatClient를 모킹하여 테스트합니다.
 */
@DisplayName("GeminiSmsParsingService 단위 테스트")
class GeminiSmsParsingServiceTest {

    @Nested
    @DisplayName("parseSms 메서드는")
    class ParseSmsTest {

        @Test
        @DisplayName("KB국민 카드 SMS를 정상적으로 파싱한다")
        void parseSms_KBCard_Success() {
            // Given
            String smsMessage = "[KB국민카드] 01/12 15:30 홍길동님 일시불 123,456원 승인\n맘스터치 강남점";
            
            String mockResponse = """
                {
                  "vendor": "KB",
                  "dateTime": "2025-01-12T15:30:00",
                  "amount": 123456,
                  "storeName": "맘스터치 강남점"
                }
                """;

            ChatClient.Builder chatClientBuilder = createMockChatClientBuilder(mockResponse);
            GeminiSmsParsingService service = new GeminiSmsParsingService(chatClientBuilder);

            // When
            SmsParsedResult result = service.parseSms(smsMessage);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.vendor()).isEqualTo("KB");
            assertThat(result.transactionDateTime()).isEqualTo(LocalDateTime.of(2025, 1, 12, 15, 30));
            assertThat(result.amount()).isEqualTo(123456L);
            assertThat(result.storeName()).isEqualTo("맘스터치 강남점");
        }

        @Test
        @DisplayName("NH농협 카드 SMS를 정상적으로 파싱한다")
        void parseSms_NHCard_Success() {
            // Given
            String smsMessage = "[NH농협카드] 승인 10/15 18:20 신한옥 님\n김밥천국 홍대점 8,500원";
            
            String mockResponse = """
                {
                  "vendor": "NH",
                  "dateTime": "2025-10-15T18:20:00",
                  "amount": 8500,
                  "storeName": "김밥천국 홍대점"
                }
                """;

            ChatClient.Builder chatClientBuilder = createMockChatClientBuilder(mockResponse);
            GeminiSmsParsingService service = new GeminiSmsParsingService(chatClientBuilder);

            // When
            SmsParsedResult result = service.parseSms(smsMessage);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.vendor()).isEqualTo("NH");
            assertThat(result.transactionDateTime()).isEqualTo(LocalDateTime.of(2025, 10, 15, 18, 20));
            assertThat(result.amount()).isEqualTo(8500L);
            assertThat(result.storeName()).isEqualTo("김밥천국 홍대점");
        }

        @Test
        @DisplayName("신한 카드 SMS를 정상적으로 파싱한다")
        void parseSms_ShinhanCard_Success() {
            // Given
            String smsMessage = "[신한카드]12/25 20:15 일시불 승인 50,000원\n스타벅스 서울역점";
            
            String mockResponse = """
                {
                  "vendor": "SH",
                  "dateTime": "2025-12-25T20:15:00",
                  "amount": 50000,
                  "storeName": "스타벅스 서울역점"
                }
                """;

            ChatClient.Builder chatClientBuilder = createMockChatClientBuilder(mockResponse);
            GeminiSmsParsingService service = new GeminiSmsParsingService(chatClientBuilder);

            // When
            SmsParsedResult result = service.parseSms(smsMessage);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.vendor()).isEqualTo("SH");
            assertThat(result.transactionDateTime()).isEqualTo(LocalDateTime.of(2025, 12, 25, 20, 15));
            assertThat(result.amount()).isEqualTo(50000L);
            assertThat(result.storeName()).isEqualTo("스타벅스 서울역점");
        }

        @Test
        @DisplayName("알 수 없는 카드사의 SMS는 UNKNOWN으로 파싱한다")
        void parseSms_UnknownVendor_Success() {
            // Given
            String smsMessage = "승인 10/12 12:00 15,000원\n맛있는 식당";
            
            String mockResponse = """
                {
                  "vendor": "UNKNOWN",
                  "dateTime": "2025-10-12T12:00:00",
                  "amount": 15000,
                  "storeName": "맛있는 식당"
                }
                """;

            ChatClient.Builder chatClientBuilder = createMockChatClientBuilder(mockResponse);
            GeminiSmsParsingService service = new GeminiSmsParsingService(chatClientBuilder);

            // When
            SmsParsedResult result = service.parseSms(smsMessage);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.vendor()).isEqualTo("UNKNOWN");
            assertThat(result.transactionDateTime()).isEqualTo(LocalDateTime.of(2025, 10, 12, 12, 0));
            assertThat(result.amount()).isEqualTo(15000L);
            assertThat(result.storeName()).isEqualTo("맛있는 식당");
        }

        @Test
        @DisplayName("빈 SMS 메시지에 대해 예외를 발생시킨다")
        void parseSms_EmptyMessage_ThrowsException() {
            // Given
            ChatClient.Builder chatClientBuilder = mock(ChatClient.Builder.class);
            GeminiSmsParsingService service = new GeminiSmsParsingService(chatClientBuilder);

            // When & Then
            assertThatThrownBy(() -> service.parseSms(""))
                    .isInstanceOf(SmsParsingException.class)
                    .hasMessageContaining("SMS 메시지가 비어있습니다");
        }

        @Test
        @DisplayName("null SMS 메시지에 대해 예외를 발생시킨다")
        void parseSms_NullMessage_ThrowsException() {
            // Given
            ChatClient.Builder chatClientBuilder = mock(ChatClient.Builder.class);
            GeminiSmsParsingService service = new GeminiSmsParsingService(chatClientBuilder);

            // When & Then
            assertThatThrownBy(() -> service.parseSms(null))
                    .isInstanceOf(SmsParsingException.class)
                    .hasMessageContaining("SMS 메시지가 비어있습니다");
        }

        @Test
        @DisplayName("공백만 있는 SMS 메시지에 대해 예외를 발생시킨다")
        void parseSms_BlankMessage_ThrowsException() {
            // Given
            ChatClient.Builder chatClientBuilder = mock(ChatClient.Builder.class);
            GeminiSmsParsingService service = new GeminiSmsParsingService(chatClientBuilder);

            // When & Then
            assertThatThrownBy(() -> service.parseSms("   "))
                    .isInstanceOf(SmsParsingException.class)
                    .hasMessageContaining("SMS 메시지가 비어있습니다");
        }

        @Test
        @DisplayName("잘못된 JSON 응답에 대해 예외를 발생시킨다")
        void parseSms_InvalidJsonResponse_ThrowsException() {
            // Given
            String smsMessage = "[KB국민카드] 테스트";
            String invalidJsonResponse = "{ invalid json }";

            ChatClient.Builder chatClientBuilder = createMockChatClientBuilder(invalidJsonResponse);
            GeminiSmsParsingService service = new GeminiSmsParsingService(chatClientBuilder);

            // When & Then
            assertThatThrownBy(() -> service.parseSms(smsMessage))
                    .isInstanceOf(SmsParsingException.class)
                    .hasMessageContaining("JSON 파싱 실패");
        }

        @Test
        @DisplayName("거래 일시가 없는 응답에 대해 예외를 발생시킨다")
        void parseSms_MissingDateTime_ThrowsException() {
            // Given
            String smsMessage = "[KB국민카드] 테스트";
            String mockResponse = """
                {
                  "vendor": "KB",
                  "dateTime": "",
                  "amount": 10000,
                  "storeName": "테스트 가게"
                }
                """;

            ChatClient.Builder chatClientBuilder = createMockChatClientBuilder(mockResponse);
            GeminiSmsParsingService service = new GeminiSmsParsingService(chatClientBuilder);

            // When & Then
            assertThatThrownBy(() -> service.parseSms(smsMessage))
                    .isInstanceOf(SmsParsingException.class)
                    .hasMessageContaining("거래 일시를 파싱할 수 없습니다");
        }

        @Test
        @DisplayName("코드 블록이 포함된 JSON 응답을 정상적으로 처리한다")
        void parseSms_JsonWithCodeBlock_Success() {
            // Given
            String smsMessage = "[KB국민카드] 테스트";
            String mockResponseWithCodeBlock = """
                ```json
                {
                  "vendor": "KB",
                  "dateTime": "2025-10-12T15:30:00",
                  "amount": 10000,
                  "storeName": "테스트 가게"
                }
                ```
                """;

            ChatClient.Builder chatClientBuilder = createMockChatClientBuilder(mockResponseWithCodeBlock);
            GeminiSmsParsingService service = new GeminiSmsParsingService(chatClientBuilder);

            // When
            SmsParsedResult result = service.parseSms(smsMessage);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.vendor()).isEqualTo("KB");
            assertThat(result.amount()).isEqualTo(10000L);
            assertThat(result.storeName()).isEqualTo("테스트 가게");
        }
    }

    /**
     * Mock ChatClient.Builder를 생성하는 헬퍼 메서드
     */
    @SuppressWarnings("unchecked")
    private ChatClient.Builder createMockChatClientBuilder(String mockResponse) {
        // Mocks 생성
        ChatClient.Builder builder = mock(ChatClient.Builder.class);
        ChatClient chatClient = mock(ChatClient.class);
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec callResponseSpec = mock(ChatClient.CallResponseSpec.class);

        // 체인 모킹: chatClient.prompt().user(prompt).call().content()
        when(builder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec); // user() returns same spec for chaining
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn(mockResponse);

        return builder;
    }
}

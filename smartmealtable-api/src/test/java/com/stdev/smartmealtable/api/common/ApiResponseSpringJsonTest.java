package com.stdev.smartmealtable.api.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stdev.smartmealtable.core.api.response.ApiResponse;
import com.stdev.smartmealtable.core.error.ErrorCode;
import com.stdev.smartmealtable.core.error.ErrorMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Spring Boot 환경에서 ApiResponse JSON 직렬화 테스트
 */
@SpringBootTest
class ApiResponseSpringJsonTest extends AbstractContainerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void success_response_should_include_null_error_in_spring_context() throws Exception {
        // given
        ApiResponse<String> response = ApiResponse.success("test data");

        // when
        String json = objectMapper.writeValueAsString(response);

        // then
        System.out.println("=== Success Response JSON ===");
        System.out.println(json);
        System.out.println("=============================");
        
        assertThat(json).contains("\"error\"");
        assertThat(json).contains("\"error\":null");
    }

    @Test
    void error_response_should_include_null_data_in_spring_context() throws Exception {
        // given
        ApiResponse<String> response = ApiResponse.error(new ErrorMessage(ErrorCode.E400, "예시 에러", null));

        // when
        String json = objectMapper.writeValueAsString(response);

        // then
        System.out.println("=== Error Response JSON ===");
        System.out.println(json);
        System.out.println("===========================");
        
        assertThat(json).contains("\"data\"");
        assertThat(json).contains("\"data\":null");
    }
}

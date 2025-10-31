package com.stdev.smartmealtable.core.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stdev.smartmealtable.core.api.response.ApiResponse;
import com.stdev.smartmealtable.core.error.ErrorCode;
import com.stdev.smartmealtable.core.error.ErrorMessage;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ApiResponse JSON 직렬화 테스트
 */
class ApiResponseJsonTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void success_response_should_include_null_error() throws Exception {
        // given
        ApiResponse<String> response = ApiResponse.success("test data");

        // when
        String json = objectMapper.writeValueAsString(response);

        // then
        System.out.println("Success Response JSON: " + json);
        assertThat(json).contains("\"error\"");
        assertThat(json).contains("\"error\":null");
    }

    @Test
    void error_response_should_include_null_data() throws Exception {
        // given
        ApiResponse<String> response = ApiResponse.error(
                ErrorMessage.of(ErrorCode.E400, "Bad Request")
        );

        // when
        String json = objectMapper.writeValueAsString(response);

        // then
        System.out.println("Error Response JSON: " + json);
        assertThat(json).contains("\"data\"");
        assertThat(json).contains("\"data\":null");
    }
}

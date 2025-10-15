package com.stdev.smartmealtable.api.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stdev.smartmealtable.api.common.AbstractContainerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 회원가입 API 테스트 (TDD RED Phase)
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("회원가입 API 테스트")
class SignupControllerTest extends AbstractContainerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("이메일 회원가입 성공 - 201 Created")
    void signup_success() throws Exception {
        // given
        Map<String, String> request = new HashMap<>();
        request.put("name", "홍길동");
        request.put("email", "hong@example.com");
        request.put("password", "SecureP@ss123!");

        // when & then
        mockMvc.perform(post("/api/v1/auth/signup/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.memberId").exists())
                .andExpect(jsonPath("$.data.email").value("hong@example.com"))
                .andExpect(jsonPath("$.data.name").value("홍길동"))
                .andExpect(jsonPath("$.error").value(nullValue()));
    }

    @Test
    @DisplayName("이메일 중복 - 409 Conflict")
    void signup_duplicateEmail() throws Exception {
        // given - 먼저 회원가입
        Map<String, String> firstRequest = new HashMap<>();
        firstRequest.put("name", "홍길동");
        firstRequest.put("email", "duplicate@example.com");
        firstRequest.put("password", "SecureP@ss123!");

        mockMvc.perform(post("/api/v1/auth/signup/email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequest)));

        // when - 같은 이메일로 다시 가입 시도
        Map<String, String> secondRequest = new HashMap<>();
        secondRequest.put("name", "김철수");
        secondRequest.put("email", "duplicate@example.com");
        secondRequest.put("password", "AnotherP@ss456!");

        // then
        mockMvc.perform(post("/api/v1/auth/signup/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondRequest)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.data").value(nullValue()))
                .andExpect(jsonPath("$.error.code").value("E409"))
                .andExpect(jsonPath("$.error.message").value("이미 사용 중인 이메일입니다."));
    }

    @Test
    @DisplayName("유효성 검증 실패 - 이메일 형식 오류 - 422 Unprocessable Entity")
    void signup_invalidEmail() throws Exception {
        // given
        Map<String, String> request = new HashMap<>();
        request.put("name", "홍길동");
        request.put("email", "invalid-email-format");  // 잘못된 이메일 형식
        request.put("password", "SecureP@ss123!");

        // when & then
        mockMvc.perform(post("/api/v1/auth/signup/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E422"));
    }

    @Test
    @DisplayName("유효성 검증 실패 - 비밀번호 형식 오류 - 422 Unprocessable Entity")
    void signup_invalidPassword() throws Exception {
        // given
        Map<String, String> request = new HashMap<>();
        request.put("name", "홍길동");
        request.put("email", "hong@example.com");
        request.put("password", "weak");  // 약한 비밀번호

        // when & then
        mockMvc.perform(post("/api/v1/auth/signup/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E422"));
    }

    @Test
    @DisplayName("유효성 검증 실패 - 필수 필드 누락 - 422 Unprocessable Entity")
    void signup_missingRequiredField() throws Exception {
        // given
        Map<String, String> request = new HashMap<>();
        request.put("name", "홍길동");
        // email, password 누락

        // when & then
        mockMvc.perform(post("/api/v1/auth/signup/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E422"));
    }

    @Test
    @DisplayName("유효성 검증 실패 - 이름 길이 제한 - 422 Unprocessable Entity")
    void signup_nameLength() throws Exception {
        // given
        Map<String, String> request = new HashMap<>();
        request.put("name", "가");  // 2자 미만
        request.put("email", "hong@example.com");
        request.put("password", "SecureP@ss123!");

        // when & then
        mockMvc.perform(post("/api/v1/auth/signup/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E422"));
    }
}

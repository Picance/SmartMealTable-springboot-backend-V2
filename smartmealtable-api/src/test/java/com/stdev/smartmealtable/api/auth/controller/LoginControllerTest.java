package com.stdev.smartmealtable.api.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 로그인 API 테스트 (TDD RED Phase)
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Testcontainers
@DisplayName("로그인 API 테스트")
class LoginControllerTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("로그인 성공 - 200 OK")
    void login_success() throws Exception {
        // given - 먼저 회원가입
        Map<String, String> signupRequest = new HashMap<>();
        signupRequest.put("name", "홍길동");
        signupRequest.put("email", "hong@example.com");
        signupRequest.put("password", "SecureP@ss123!");

        mockMvc.perform(post("/api/v1/auth/signup/email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)));

        // when - 로그인 요청
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", "hong@example.com");
        loginRequest.put("password", "SecureP@ss123!");

        // then
        mockMvc.perform(post("/api/v1/auth/login/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists())
                .andExpect(jsonPath("$.data.memberId").exists())
                .andExpect(jsonPath("$.data.email").value("hong@example.com"))
                .andExpect(jsonPath("$.data.name").value("홍길동"))
                .andExpect(jsonPath("$.data.onboardingComplete").value(false))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 이메일 - 401 Unauthorized")
    void login_emailNotFound() throws Exception {
        // given
        Map<String, String> request = new HashMap<>();
        request.put("email", "notfound@example.com");
        request.put("password", "SecureP@ss123!");

        // when & then
        mockMvc.perform(post("/api/v1/auth/login/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E401"))
                .andExpect(jsonPath("$.error.message").value("이메일 또는 비밀번호가 일치하지 않습니다."))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 비밀번호 - 401 Unauthorized")
    void login_invalidPassword() throws Exception {
        // given - 먼저 회원가입
        Map<String, String> signupRequest = new HashMap<>();
        signupRequest.put("name", "홍길동");
        signupRequest.put("email", "hong2@example.com");
        signupRequest.put("password", "SecureP@ss123!");

        mockMvc.perform(post("/api/v1/auth/signup/email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)));

        // when - 잘못된 비밀번호로 로그인
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", "hong2@example.com");
        loginRequest.put("password", "WrongPassword!");

        // then
        mockMvc.perform(post("/api/v1/auth/login/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E401"))
                .andExpect(jsonPath("$.error.message").value("이메일 또는 비밀번호가 일치하지 않습니다."))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("로그인 실패 - 유효성 검증 실패 (이메일 형식 오류) - 422 Unprocessable Entity")
    void login_invalidEmailFormat() throws Exception {
        // given
        Map<String, String> request = new HashMap<>();
        request.put("email", "invalid-email-format");
        request.put("password", "SecureP@ss123!");

        // when & then
        mockMvc.perform(post("/api/v1/auth/login/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E422"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("로그인 실패 - 필수 필드 누락 - 422 Unprocessable Entity")
    void login_missingRequiredField() throws Exception {
        // given
        Map<String, String> request = new HashMap<>();
        request.put("email", "hong@example.com");
        // password 누락

        // when & then
        mockMvc.perform(post("/api/v1/auth/login/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E422"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("토큰 재발급 성공 - 200 OK")
    void refreshToken_success() throws Exception {
        // given - 먼저 회원가입 및 로그인하여 refresh token 획득
        Map<String, String> signupRequest = new HashMap<>();
        signupRequest.put("name", "홍길동");
        signupRequest.put("email", "refresh@example.com");
        signupRequest.put("password", "SecureP@ss123!");

        mockMvc.perform(post("/api/v1/auth/signup/email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)));

        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", "refresh@example.com");
        loginRequest.put("password", "SecureP@ss123!");

        String loginResponseJson = mockMvc.perform(post("/api/v1/auth/login/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // JSON에서 refreshToken 추출
        String refreshToken = objectMapper.readTree(loginResponseJson)
                .get("data").get("refreshToken").asText();

        // when - 토큰 재발급 요청
        Map<String, String> refreshRequest = new HashMap<>();
        refreshRequest.put("refreshToken", refreshToken);

        // then
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists())
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    @DisplayName("토큰 재발급 실패 - 유효하지 않은 Refresh Token - 401 Unauthorized")
    void refreshToken_invalidToken() throws Exception {
        // given
        Map<String, String> request = new HashMap<>();
        request.put("refreshToken", "invalid.jwt.token");

        // when & then
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E401"))
                .andExpect(jsonPath("$.error.message").value("유효하지 않은 Refresh Token입니다."))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("토큰 재발급 실패 - Access Token으로 재발급 시도 - 401 Unauthorized")
    void refreshToken_accessTokenProvided() throws Exception {
        // given - 먼저 로그인하여 access token 획득
        Map<String, String> signupRequest = new HashMap<>();
        signupRequest.put("name", "홍길동");
        signupRequest.put("email", "access@example.com");
        signupRequest.put("password", "SecureP@ss123!");

        mockMvc.perform(post("/api/v1/auth/signup/email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)));

        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", "access@example.com");
        loginRequest.put("password", "SecureP@ss123!");

        String loginResponseJson = mockMvc.perform(post("/api/v1/auth/login/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // JSON에서 accessToken 추출
        String accessToken = objectMapper.readTree(loginResponseJson)
                .get("data").get("accessToken").asText();

        // when - access token으로 재발급 시도
        Map<String, String> refreshRequest = new HashMap<>();
        refreshRequest.put("refreshToken", accessToken);

        // then
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E401"))
                .andExpect(jsonPath("$.error.message").value("유효하지 않은 Refresh Token입니다."))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("로그아웃 성공 - 200 OK")
    void logout_success() throws Exception {
        // given - 먼저 회원가입 및 로그인하여 access token 획득
        Map<String, String> signupRequest = new HashMap<>();
        signupRequest.put("name", "로그아웃테스트");
        signupRequest.put("email", "logout@example.com");
        signupRequest.put("password", "SecureP@ss123!");

        mockMvc.perform(post("/api/v1/auth/signup/email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)));

        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", "logout@example.com");
        loginRequest.put("password", "SecureP@ss123!");

        String loginResponseJson = mockMvc.perform(post("/api/v1/auth/login/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // JSON에서 accessToken 추출
        String accessToken = objectMapper.readTree(loginResponseJson)
                .get("data").get("accessToken").asText();

        // when & then - 로그아웃 요청
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    @DisplayName("로그아웃 실패 - Authorization 헤더 없음 - 401 Unauthorized")
    void logout_noAuthorizationHeader() throws Exception {
        // when & then
        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E401"))
                .andExpect(jsonPath("$.error.message").value("유효하지 않은 토큰입니다."))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("로그아웃 실패 - 유효하지 않은 Access Token - 401 Unauthorized")
    void logout_invalidToken() throws Exception {
        // when & then
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer invalid.jwt.token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E401"))
                .andExpect(jsonPath("$.error.message").value("유효하지 않은 토큰입니다."))
                .andExpect(jsonPath("$.data").doesNotExist());
    }
}
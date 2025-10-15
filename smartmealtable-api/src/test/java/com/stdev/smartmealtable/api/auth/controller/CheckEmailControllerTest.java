package com.stdev.smartmealtable.api.auth.controller;

import com.stdev.smartmealtable.api.auth.service.SignupService;
import com.stdev.smartmealtable.api.common.AbstractContainerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 이메일 중복 검증 API 테스트 (TDD)
 * AbstractContainerTest를 상속하여 공유 MySQL 컨테이너 사용
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("이메일 중복 검증 API 테스트")
class CheckEmailControllerTest extends AbstractContainerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SignupService signupService;

    @Test
    @DisplayName("이메일 중복 검증 - 사용 가능한 이메일 (200 OK)")
    void checkEmail_available() throws Exception {
        // given
        String email = "available@example.com";

        // when & then
        mockMvc.perform(get("/api/v1/auth/check-email")
                        .param("email", email))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.available").value(true))
                .andExpect(jsonPath("$.data.message").value("사용 가능한 이메일입니다."))
                .andExpect(jsonPath("$.error").value(nullValue()));
    }

    @Test
    @DisplayName("이메일 중복 검증 - 이미 사용 중인 이메일 (200 OK)")
    void checkEmail_duplicate() throws Exception {
        // given - 회원가입으로 이메일 먼저 등록
        String email = "duplicate@example.com";
        signupService.signup(new com.stdev.smartmealtable.api.auth.service.dto.SignupServiceRequest(
                "테스트",
                email,
                "SecureP@ss123!"
        ));

        // when & then
        mockMvc.perform(get("/api/v1/auth/check-email")
                        .param("email", email))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.available").value(false))
                .andExpect(jsonPath("$.data.message").value("이미 사용 중인 이메일입니다."))
                .andExpect(jsonPath("$.error").value(nullValue()));
    }

    @Test
    @DisplayName("이메일 중복 검증 - 잘못된 이메일 형식 (422 Unprocessable Entity)")
    void checkEmail_invalidFormat() throws Exception {
        // given
        String invalidEmail = "invalid-email";

        // when & then
        mockMvc.perform(get("/api/v1/auth/check-email")
                        .param("email", invalidEmail))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E422"))
                .andExpect(jsonPath("$.error.message").value("이메일 형식이 올바르지 않습니다."));
    }
}

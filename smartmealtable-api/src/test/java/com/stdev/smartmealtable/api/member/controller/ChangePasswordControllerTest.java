package com.stdev.smartmealtable.api.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stdev.smartmealtable.api.auth.service.SignupService;
import com.stdev.smartmealtable.api.auth.service.dto.SignupServiceRequest;
import com.stdev.smartmealtable.api.common.AbstractContainerTest;
import com.stdev.smartmealtable.support.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 비밀번호 변경 API 테스트 (TDD)
 * AbstractContainerTest를 상속하여 공유 MySQL 컨테이너 사용
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("비밀번호 변경 API 테스트")
class ChangePasswordControllerTest extends AbstractContainerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SignupService signupService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private Long testMemberId;
    private String testEmail = "test@example.com";
    private String currentPassword = "OldP@ss123!";
    private String accessToken;

    @BeforeEach
    void setUp() {
        // 테스트용 회원 생성
        var response = signupService.signup(new SignupServiceRequest(
                "테스트",
                testEmail,
                currentPassword
        ));
        testMemberId = response.getMemberId();
        
        // JWT 토큰 생성
        accessToken = jwtTokenProvider.createToken(testMemberId);
    }

    @Test
    @DisplayName("비밀번호 변경 성공 - 200 OK")
    void changePassword_success() throws Exception {
        // given
        Map<String, String> request = new HashMap<>();
        request.put("currentPassword", currentPassword);
        request.put("newPassword", "NewP@ss456!");

        // when & then
        mockMvc.perform(put("/api/v1/members/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.message").value("비밀번호가 변경되었습니다."))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 현재 비밀번호 불일치 (401 Unauthorized)")
    void changePassword_wrongCurrentPassword() throws Exception {
        // given
        Map<String, String> request = new HashMap<>();
        request.put("currentPassword", "WrongP@ss123!");
        request.put("newPassword", "NewP@ss456!");

        // when & then
        mockMvc.perform(put("/api/v1/members/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E401"))
                .andExpect(jsonPath("$.error.message").value("현재 비밀번호가 일치하지 않습니다."));
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 새 비밀번호 형식 오류 (422 Unprocessable Entity)")
    void changePassword_invalidNewPasswordFormat() throws Exception {
        // given
        Map<String, String> request = new HashMap<>();
        request.put("currentPassword", currentPassword);
        request.put("newPassword", "weak"); // 형식 위반

        // when & then
        mockMvc.perform(put("/api/v1/members/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E422"))
                .andExpect(jsonPath("$.error.message").exists()); // 메시지가 존재하면 OK
    }
}

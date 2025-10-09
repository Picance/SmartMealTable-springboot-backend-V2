package com.stdev.smartmealtable.api.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stdev.smartmealtable.api.auth.service.SignupService;
import com.stdev.smartmealtable.api.auth.service.dto.SignupServiceRequest;
import com.stdev.smartmealtable.api.common.AbstractContainerTest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 회원 탈퇴 API 테스트 (TDD)
 * AbstractContainerTest를 상속하여 공유 MySQL 컨테이너 사용
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("회원 탈퇴 API 테스트")
class WithdrawMemberControllerTest extends AbstractContainerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SignupService signupService;

    private Long testMemberId;
    private String testPassword = "TestP@ss123!";

    @BeforeEach
    void setUp() {
        var response = signupService.signup(new SignupServiceRequest(
                "테스트",
                "test@example.com",
                testPassword
        ));
        testMemberId = response.getMemberId();
    }

    @Test
    @DisplayName("회원 탈퇴 성공 - 204 No Content")
    void withdrawMember_success() throws Exception {
        // given
        Map<String, String> request = new HashMap<>();
        request.put("password", testPassword);
        request.put("reason", "서비스 불만족");

        // when & then
        mockMvc.perform(delete("/api/v1/members/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-Member-Id", testMemberId))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("회원 탈퇴 실패 - 비밀번호 불일치 (401 Unauthorized)")
    void withdrawMember_wrongPassword() throws Exception {
        // given
        Map<String, String> request = new HashMap<>();
        request.put("password", "WrongP@ss123!");
        request.put("reason", "서비스 불만족");

        // when & then
        mockMvc.perform(delete("/api/v1/members/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-Member-Id", testMemberId))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E401"))
                .andExpect(jsonPath("$.error.message").value("현재 비밀번호가 일치하지 않습니다."));
    }
}

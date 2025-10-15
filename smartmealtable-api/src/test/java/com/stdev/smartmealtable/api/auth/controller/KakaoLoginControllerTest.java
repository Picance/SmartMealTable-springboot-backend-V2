package com.stdev.smartmealtable.api.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stdev.smartmealtable.api.common.AbstractContainerTest;
import com.stdev.smartmealtable.client.auth.oauth.kakao.KakaoAuthClient;
import com.stdev.smartmealtable.client.auth.oauth.dto.OAuthTokenResponse;
import com.stdev.smartmealtable.client.auth.oauth.dto.OAuthUserInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 카카오 소셜 로그인 API 통합 테스트
 * AbstractContainerTest를 상속하여 공유 MySQL 컨테이너 사용
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("카카오 소셜 로그인 API 통합 테스트")
class KakaoLoginControllerTest extends AbstractContainerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private KakaoAuthClient kakaoAuthClient;

    @Test
    @DisplayName("카카오 로그인 성공 - 신규 회원 - 200 OK")
    void kakaoLogin_newMember_success() throws Exception {
        // given - 카카오 OAuth 응답 모킹
        OAuthTokenResponse tokenResponse = new OAuthTokenResponse(
                "mock-access-token",
                "mock-refresh-token",
                3600,
                "Bearer",
                "mock-id-token"
        );
        OAuthUserInfo userInfo = new OAuthUserInfo(
                "12345678",
                "kakao_user@example.com",
                "카카오유저",
                "https://kakao.com/profile.jpg"
        );

        given(kakaoAuthClient.getAccessToken(anyString()))
                .willReturn(tokenResponse);
        given(kakaoAuthClient.extractUserInfo(anyString()))
                .willReturn(userInfo);

        // when - 카카오 로그인 요청
        Map<String, String> request = new HashMap<>();
        request.put("authorizationCode", "mock-authorization-code");
        request.put("redirectUri", "http://localhost:3000/auth/kakao/callback");

        // then
        mockMvc.perform(post("/api/v1/auth/login/kakao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.memberId").exists())
                .andExpect(jsonPath("$.data.email").value("kakao_user@example.com"))
                .andExpect(jsonPath("$.data.name").value("카카오유저"))
                .andExpect(jsonPath("$.data.profileImageUrl").value("https://kakao.com/profile.jpg"))
                .andExpect(jsonPath("$.data.isNewMember").value(true))
                .andExpect(jsonPath("$.error").value(nullValue()));
    }

    @Test
    @DisplayName("카카오 로그인 성공 - 기존 회원 - 200 OK")
    void kakaoLogin_existingMember_success() throws Exception {
        // given - 첫 번째 로그인 (신규 회원 생성)
        OAuthTokenResponse tokenResponse = new OAuthTokenResponse(
                "mock-access-token",
                "mock-refresh-token",
                3600,
                "Bearer",
                "mock-id-token"
        );
        OAuthUserInfo userInfo = new OAuthUserInfo(
                "87654321",
                "existing@example.com",
                "기존유저",
                "https://kakao.com/profile.jpg"
        );

        given(kakaoAuthClient.getAccessToken(anyString()))
                .willReturn(tokenResponse);
        given(kakaoAuthClient.extractUserInfo(anyString()))
                .willReturn(userInfo);

        Map<String, String> request = new HashMap<>();
        request.put("authorizationCode", "mock-authorization-code");
        request.put("redirectUri", "http://localhost:3000/auth/kakao/callback");

        // 첫 번째 로그인 (신규 회원 생성)
        mockMvc.perform(post("/api/v1/auth/login/kakao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isNewMember").value(true));

        // when - 두 번째 로그인 (기존 회원)
        OAuthUserInfo updatedUserInfo = new OAuthUserInfo(
                "87654321",  // 동일한 provider_id
                "updated@example.com",  // 이메일 변경
                "변경된이름",  // 이름 변경
                "https://kakao.com/new-profile.jpg"  // 프로필 이미지 변경
        );

        given(kakaoAuthClient.extractUserInfo(anyString()))
                .willReturn(updatedUserInfo);

        // then
        mockMvc.perform(post("/api/v1/auth/login/kakao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.memberId").exists())
                .andExpect(jsonPath("$.data.email").value("updated@example.com"))
                .andExpect(jsonPath("$.data.name").value("변경된이름"))
                .andExpect(jsonPath("$.data.profileImageUrl").value("https://kakao.com/new-profile.jpg"))
                .andExpect(jsonPath("$.data.isNewMember").value(false))
                .andExpect(jsonPath("$.error").value(nullValue()));
    }

    @Test
    @DisplayName("카카오 로그인 실패 - authorizationCode 누락 - 422 Unprocessable Entity")
    void kakaoLogin_missingCode_fail() throws Exception {
        // given
        Map<String, String> request = new HashMap<>();
        request.put("redirectUri", "http://localhost:3000/auth/kakao/callback");

        // when & then
        mockMvc.perform(post("/api/v1/auth/login/kakao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error.code").value("E422"))
                .andExpect(jsonPath("$.error.message").exists())
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    @Test
    @DisplayName("카카오 로그인 실패 - redirectUri 누락 - 422 Unprocessable Entity")
    void kakaoLogin_missingRedirectUri_fail() throws Exception {
        // given
        Map<String, String> request = new HashMap<>();
        request.put("authorizationCode", "mock-authorization-code");

        // when & then
        mockMvc.perform(post("/api/v1/auth/login/kakao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error.code").value("E422"))
                .andExpect(jsonPath("$.error.message").exists())
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    @Test
    @DisplayName("카카오 로그인 실패 - 빈 문자열 authorizationCode - 422 Unprocessable Entity")
    void kakaoLogin_emptyCode_fail() throws Exception {
        // given
        Map<String, String> request = new HashMap<>();
        request.put("authorizationCode", "");
        request.put("redirectUri", "http://localhost:3000/auth/kakao/callback");

        // when & then
        mockMvc.perform(post("/api/v1/auth/login/kakao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error.code").value("E422"))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }
}

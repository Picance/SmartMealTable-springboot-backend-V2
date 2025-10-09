package com.stdev.smartmealtable.api.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stdev.smartmealtable.api.common.AbstractContainerTest;
import com.stdev.smartmealtable.api.auth.dto.request.GoogleLoginServiceRequest;
import com.stdev.smartmealtable.client.auth.oauth.google.GoogleAuthClient;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 구글 소셜 로그인 API 통합 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("구글 소셜 로그인 API 통합 테스트")
class GoogleLoginControllerTest extends AbstractContainerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GoogleAuthClient googleAuthClient;

    @Test
    @DisplayName("구글 로그인 성공 - 신규 회원 - 200 OK")
    void shouldReturnNewMemberResponse_whenGoogleLoginWithNewUser() throws Exception {
        // given: 구글 Access Token 및 ID Token 발급 Mock
        OAuthTokenResponse tokenResponse = new OAuthTokenResponse(
                "mock-access-token",
                "mock-refresh-token",
                3600,
                "Bearer",
                "mock-id-token"
        );
        given(googleAuthClient.getAccessToken(anyString()))
                .willReturn(tokenResponse);

        // given: ID Token에서 사용자 정보 추출 Mock (신규 회원)
        Map<String, Object> userInfoMap = new HashMap<>();
        userInfoMap.put("id", "12345678");
        userInfoMap.put("email", "google_user@example.com");
        userInfoMap.put("name", "구글유저");
        userInfoMap.put("picture", "https://google.com/profile.jpg");

        OAuthUserInfo userInfo = new OAuthUserInfo(
                "12345678",
                "google_user@example.com",
                "구글유저",
                "https://google.com/profile.jpg"
        );
        given(googleAuthClient.extractUserInfo(anyString())).willReturn(userInfo);

        GoogleLoginServiceRequest request = new GoogleLoginServiceRequest(
                "mock-authorization-code",
                "http://localhost:3000/auth/google/callback"
        );

        // when & then
        mockMvc.perform(post("/api/v1/auth/login/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.memberId").isNumber())
                .andExpect(jsonPath("$.data.email").value("google_user@example.com"))
                .andExpect(jsonPath("$.data.name").value("구글유저"))
                .andExpect(jsonPath("$.data.profileImageUrl").value("https://google.com/profile.jpg"))
                .andExpect(jsonPath("$.data.isNewMember").value(true));
    }

    @Test
    @DisplayName("구글 로그인 성공 - 기존 회원 - 200 OK")
    void shouldReturnExistingMemberResponse_whenGoogleLoginWithExistingUser() throws Exception {
        // given: 구글 Access Token 및 ID Token 발급 Mock
        OAuthTokenResponse tokenResponse = new OAuthTokenResponse(
                "mock-access-token",
                "mock-refresh-token",
                3600,
                "Bearer",
                "mock-id-token"
        );
        given(googleAuthClient.getAccessToken(anyString()))
                .willReturn(tokenResponse);

        // given: ID Token에서 사용자 정보 추출 Mock (기존 회원이지만 정보 변경)
        OAuthUserInfo userInfo = new OAuthUserInfo(
                "87654321",
                "updated@example.com",
                "변경된이름",
                "https://google.com/new-profile.jpg"
        );
        given(googleAuthClient.extractUserInfo(anyString())).willReturn(userInfo);

        GoogleLoginServiceRequest request = new GoogleLoginServiceRequest(
                "mock-authorization-code",
                "http://localhost:3000/auth/google/callback"
        );

        // when & then: 먼저 한 번 로그인하여 기존 회원 데이터 생성
        mockMvc.perform(post("/api/v1/auth/login/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.isNewMember").value(true));

        // when & then: 동일한 사용자로 다시 로그인 (기존 회원)
        mockMvc.perform(post("/api/v1/auth/login/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.memberId").isNumber())
                .andExpect(jsonPath("$.data.email").value("updated@example.com"))
                .andExpect(jsonPath("$.data.name").value("변경된이름"))
                .andExpect(jsonPath("$.data.profileImageUrl").value("https://google.com/new-profile.jpg"))
                .andExpect(jsonPath("$.data.isNewMember").value(false));
    }

    @Test
    @DisplayName("구글 로그인 실패 - authorizationCode 누락 - 422 Unprocessable Entity")
    void shouldReturn422_whenAuthorizationCodeIsMissing() throws Exception {
        // given
        GoogleLoginServiceRequest request = new GoogleLoginServiceRequest(
                null, // authorizationCode 누락
                "http://localhost:3000/auth/google/callback"
        );

        // when & then
        mockMvc.perform(post("/api/v1/auth/login/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E422"))
                .andExpect(jsonPath("$.error.message").value("Authorization code는 필수입니다."))
                .andExpect(jsonPath("$.error.data.field").value("authorizationCode"))
                .andExpect(jsonPath("$.error.data.reason").value("Authorization code는 필수입니다."));
    }

    @Test
    @DisplayName("구글 로그인 실패 - redirectUri 누락 - 422 Unprocessable Entity")
    void shouldReturn422_whenRedirectUriIsMissing() throws Exception {
        // given
        GoogleLoginServiceRequest request = new GoogleLoginServiceRequest(
                "mock-authorization-code",
                null // redirectUri 누락
        );

        // when & then
        mockMvc.perform(post("/api/v1/auth/login/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E422"))
                .andExpect(jsonPath("$.error.message").value("Redirect URI는 필수입니다."))
                .andExpect(jsonPath("$.error.data.field").value("redirectUri"))
                .andExpect(jsonPath("$.error.data.reason").value("Redirect URI는 필수입니다."));
    }

    @Test
    @DisplayName("구글 로그인 실패 - 빈 문자열 authorizationCode - 422 Unprocessable Entity")
    void shouldReturn422_whenAuthorizationCodeIsBlank() throws Exception {
        // given
        GoogleLoginServiceRequest request = new GoogleLoginServiceRequest(
                "", // 빈 문자열
                "http://localhost:3000/auth/google/callback"
        );

        // when & then
        mockMvc.perform(post("/api/v1/auth/login/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E422"))
                .andExpect(jsonPath("$.error.message").value("Authorization code는 필수입니다."))
                .andExpect(jsonPath("$.error.data.field").value("authorizationCode"))
                .andExpect(jsonPath("$.error.data.reason").value("Authorization code는 필수입니다."));
    }
}

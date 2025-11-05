package com.stdev.smartmealtable.api.auth.controller;

import com.stdev.smartmealtable.api.common.AbstractRestDocsTest;
import com.stdev.smartmealtable.client.auth.oauth.dto.OAuthTokenResponse;
import com.stdev.smartmealtable.client.auth.oauth.dto.OAuthUserInfo;
import com.stdev.smartmealtable.client.auth.oauth.kakao.KakaoAuthClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 카카오 소셜 로그인 API Rest Docs 테스트
 */
@DisplayName("카카오 소셜 로그인 API Rest Docs")
@Transactional
class KakaoLoginControllerRestDocsTest extends AbstractRestDocsTest {

    @MockBean
    private KakaoAuthClient kakaoAuthClient;

    @Test
    @DisplayName("카카오 로그인 성공 - 신규 회원 문서화")
    void kakaoLogin_newMember_docs() throws Exception {
        // given - 카카오 OAuth 응답 모킹
        OAuthTokenResponse tokenResponse = new OAuthTokenResponse(
                "kakao-access-token",
                "kakao-refresh-token",
                3600,
                "Bearer",
                "kakao-id-token"
        );
        OAuthUserInfo userInfo = new OAuthUserInfo(
                "12345678",
                "kakao_user@example.com",
                "카카오사용자",
                "https://kakao.com/profile.jpg"
        );

        given(kakaoAuthClient.getAccessToken(anyString()))
                .willReturn(tokenResponse);
        given(kakaoAuthClient.extractUserInfo(anyString()))
                .willReturn(userInfo);

        // when
        Map<String, String> request = new HashMap<>();
        request.put("authorizationCode", "kakao-authorization-code");
        request.put("redirectUri", "http://localhost:3000/auth/kakao/callback");

        // then
        mockMvc.perform(post("/api/v1/auth/login/kakao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andDo(document("auth/login/kakao/new-member",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                                fieldWithPath("authorizationCode")
                                        .type(JsonFieldType.STRING)
                                        .description("카카오 인가 코드 (카카오 로그인 완료 후 받은 코드)"),
                                fieldWithPath("redirectUri")
                                        .type(JsonFieldType.STRING)
                                        .description("리다이렉트 URI (카카오 앱 설정과 동일해야 함)")
                        ),
                        responseFields(
                                fieldWithPath("result")
                                        .type(JsonFieldType.STRING)
                                        .description("결과 상태 (SUCCESS)"),
                                fieldWithPath("data")
                                        .type(JsonFieldType.OBJECT)
                                        .description("응답 데이터"),
                                fieldWithPath("data.accessToken")
                                        .type(JsonFieldType.STRING)
                                        .description("JWT 액세스 토큰"),
                                fieldWithPath("data.refreshToken")
                                        .type(JsonFieldType.STRING)
                                        .description("JWT 리프레시 토큰"),
                                fieldWithPath("data.memberId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("회원 ID"),
                                fieldWithPath("data.email")
                                        .type(JsonFieldType.STRING)
                                        .description("이메일 주소"),
                                fieldWithPath("data.name")
                                        .type(JsonFieldType.STRING)
                                        .description("사용자 이름"),
                                fieldWithPath("data.profileImageUrl")
                                        .type(JsonFieldType.STRING)
                                        .description("프로필 이미지 URL"),
                                fieldWithPath("data.isNewMember")
                                        .type(JsonFieldType.BOOLEAN)
                                        .description("신규 회원 여부 (true: 신규, false: 기존)"),
                                fieldWithPath("error")
                                        .type(JsonFieldType.NULL)
                                        .description("에러 정보 (성공 시 null)")
                                        .optional()
                        )
                ));
    }

    @Test
    @DisplayName("카카오 로그인 실패 - 유효하지 않은 인가 코드 문서화")
    void kakaoLogin_invalidCode_docs() throws Exception {
        // given - 카카오 OAuth 실패 모킹
        given(kakaoAuthClient.getAccessToken(anyString()))
                .willThrow(new RuntimeException("Invalid authorization code"));

        // when
        Map<String, String> request = new HashMap<>();
        request.put("authorizationCode", "invalid-authorization-code");
        request.put("redirectUri", "http://localhost:3000/auth/kakao/callback");

        // then
        mockMvc.perform(post("/api/v1/auth/login/kakao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andDo(document("auth/login/kakao/invalid-code",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                                fieldWithPath("authorizationCode")
                                        .type(JsonFieldType.STRING)
                                        .description("유효하지 않은 카카오 인가 코드"),
                                fieldWithPath("redirectUri")
                                        .type(JsonFieldType.STRING)
                                        .description("리다이렉트 URI")
                        ),
                        responseFields(
                                fieldWithPath("result")
                                        .type(JsonFieldType.STRING)
                                        .description("결과 상태 (ERROR)"),
                                fieldWithPath("data")
                                        .type(JsonFieldType.NULL)
                                        .description("응답 데이터 (에러 시 null)")
                                        .optional(),
                                fieldWithPath("error")
                                        .type(JsonFieldType.OBJECT)
                                        .description("에러 정보"),
                                fieldWithPath("error.code")
                                        .type(JsonFieldType.STRING)
                                        .description("에러 코드 (E401)"),
                                fieldWithPath("error.message")
                                        .type(JsonFieldType.STRING)
                                        .description("에러 메시지"),
                                fieldWithPath("error.data")
                                        .type(JsonFieldType.NULL)
                                        .description("에러 상세 데이터")
                                        .optional()
                        )
                ));
    }

    @Test
    @DisplayName("카카오 로그인 실패 - 빈 인가 코드 문서화")
    void kakaoLogin_emptyCode_docs() throws Exception {
        // when
        Map<String, String> request = new HashMap<>();
        request.put("authorizationCode", "");
        request.put("redirectUri", "http://localhost:3000/auth/kakao/callback");

        // then
        mockMvc.perform(post("/api/v1/auth/login/kakao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andDo(document("auth/login/kakao/empty-code",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                                fieldWithPath("authorizationCode")
                                        .type(JsonFieldType.STRING)
                                        .description("빈 인가 코드 (유효성 검증 실패)"),
                                fieldWithPath("redirectUri")
                                        .type(JsonFieldType.STRING)
                                        .description("리다이렉트 URI")
                        ),
                        responseFields(
                                fieldWithPath("result")
                                        .type(JsonFieldType.STRING)
                                        .description("결과 상태 (ERROR)"),
                                fieldWithPath("data")
                                        .type(JsonFieldType.NULL)
                                        .description("응답 데이터 (에러 시 null)")
                                        .optional(),
                                fieldWithPath("error")
                                        .type(JsonFieldType.OBJECT)
                                        .description("에러 정보"),
                                fieldWithPath("error.code")
                                        .type(JsonFieldType.STRING)
                                        .description("에러 코드 (E422)"),
                                fieldWithPath("error.message")
                                        .type(JsonFieldType.STRING)
                                        .description("에러 메시지"),
                                fieldWithPath("error.data")
                                        .type(JsonFieldType.OBJECT)
                                        .description("에러 상세 데이터"),
                                fieldWithPath("error.data.field")
                                        .type(JsonFieldType.STRING)
                                        .description("검증 실패한 필드명"),
                                fieldWithPath("error.data.reason")
                                        .type(JsonFieldType.STRING)
                                        .description("검증 실패 이유")
                        )
                ));
    }
}

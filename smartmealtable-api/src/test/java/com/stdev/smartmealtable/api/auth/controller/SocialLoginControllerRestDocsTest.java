package com.stdev.smartmealtable.api.auth.controller;

import com.stdev.smartmealtable.api.auth.dto.request.GoogleLoginServiceRequest;
import com.stdev.smartmealtable.api.auth.dto.request.KakaoLoginServiceRequest;
import com.stdev.smartmealtable.api.auth.service.GoogleLoginService;
import com.stdev.smartmealtable.api.auth.service.KakaoLoginService;
import com.stdev.smartmealtable.api.common.AbstractRestDocsTest;
import com.stdev.smartmealtable.api.auth.dto.response.GoogleLoginServiceResponse;
import com.stdev.smartmealtable.api.auth.dto.response.KakaoLoginServiceResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 소셜 로그인 API REST Docs 테스트
 * 
 * Google 및 Kakao 소셜 로그인 엔드포인트의 성공/실패 시나리오를 문서화합니다.
 */
@DisplayName("SocialLoginController REST Docs 테스트")
@Transactional
class SocialLoginControllerRestDocsTest extends AbstractRestDocsTest {

    @MockBean
    private GoogleLoginService googleLoginService;

    @MockBean
    private KakaoLoginService kakaoLoginService;

    @Test
    @DisplayName("카카오 로그인 성공 - 신규 회원")
    void kakaoLogin_newMember_success_docs() throws Exception {
        // given - 카카오 로그인 성공 응답
        KakaoLoginServiceResponse response = new KakaoLoginServiceResponse(
                "kakao-access-token-jwt",
                "kakao-refresh-token-jwt",
                1001L,
                "kakao_user@example.com",
                "카카오사용자",
                "https://kakao.com/profile.jpg",
                true  // isNewMember
        );

        given(kakaoLoginService.login(any()))
                .willReturn(response);

        // when
        String requestBody = """
                {
                    "authorizationCode": "kakao-auth-code-abc123",
                    "redirectUri": "http://localhost:3000/auth/kakao/callback"
                }
                """;

        // then - 신규 회원 성공 시나리오 문서화
        mockMvc.perform(post("/api/v1/auth/login/kakao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.isNewMember").value(true))
                .andDo(document("auth/login/kakao/new-member-success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                                fieldWithPath("authorizationCode")
                                        .type(JsonFieldType.STRING)
                                        .description("카카오 OAuth 인가 코드"),
                                fieldWithPath("redirectUri")
                                        .type(JsonFieldType.STRING)
                                        .description("리다이렉트 URI (카카오 앱 설정과 일치해야 함)")
                        ),
                        responseFields(
                                fieldWithPath("result")
                                        .type(JsonFieldType.STRING)
                                        .description("응답 결과 코드 (SUCCESS)"),
                                fieldWithPath("data")
                                        .type(JsonFieldType.OBJECT)
                                        .description("로그인 결과 데이터"),
                                fieldWithPath("data.accessToken")
                                        .type(JsonFieldType.STRING)
                                        .description("JWT 액세스 토큰 (API 호출 시 Authorization 헤더에 사용)"),
                                fieldWithPath("data.refreshToken")
                                        .type(JsonFieldType.STRING)
                                        .description("JWT 리프레시 토큰 (액세스 토큰 갱신 시 사용)"),
                                fieldWithPath("data.memberId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("회원 ID"),
                                fieldWithPath("data.email")
                                        .type(JsonFieldType.STRING)
                                        .description("이메일 주소 (카카오 계정 이메일)"),
                                fieldWithPath("data.name")
                                        .type(JsonFieldType.STRING)
                                        .description("사용자명 (카카오 계정 이름)"),
                                fieldWithPath("data.profileImageUrl")
                                        .type(JsonFieldType.STRING)
                                        .description("프로필 이미지 URL"),
                                fieldWithPath("data.isNewMember")
                                        .type(JsonFieldType.BOOLEAN)
                                        .description("신규 회원 여부 (true: 신규 가입, false: 기존 회원)"),
                                fieldWithPath("error")
                                        .type(JsonFieldType.NULL)
                                        .optional()
                                        .description("에러 정보 (성공 시 null)")
                        )
                ));
    }

    @Test
    @DisplayName("카카오 로그인 성공 - 기존 회원")
    void kakaoLogin_existingMember_success_docs() throws Exception {
        // given - 카카오 로그인 기존 회원 응답
        KakaoLoginServiceResponse response = new KakaoLoginServiceResponse(
                "kakao-access-token-jwt-existing",
                "kakao-refresh-token-jwt-existing",
                1002L,
                "kakao_existing@example.com",
                "카카오기존사용자",
                "https://kakao.com/profile2.jpg",
                false  // isNewMember (기존 회원)
        );

        given(kakaoLoginService.login(any()))
                .willReturn(response);

        // when
        String requestBody = """
                {
                    "authorizationCode": "kakao-auth-code-def456",
                    "redirectUri": "http://localhost:3000/auth/kakao/callback"
                }
                """;

        // then - 기존 회원 로그인 시나리오 문서화
        mockMvc.perform(post("/api/v1/auth/login/kakao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.isNewMember").value(false))
                .andDo(document("auth/login/kakao/existing-member-success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                                fieldWithPath("authorizationCode")
                                        .type(JsonFieldType.STRING)
                                        .description("카카오 OAuth 인가 코드"),
                                fieldWithPath("redirectUri")
                                        .type(JsonFieldType.STRING)
                                        .description("리다이렉트 URI (카카오 앱 설정과 일치해야 함)")
                        ),
                        responseFields(
                                fieldWithPath("result")
                                        .type(JsonFieldType.STRING)
                                        .description("응답 결과 코드 (SUCCESS)"),
                                fieldWithPath("data")
                                        .type(JsonFieldType.OBJECT)
                                        .description("로그인 결과 데이터"),
                                fieldWithPath("data.accessToken")
                                        .type(JsonFieldType.STRING)
                                        .description("JWT 액세스 토큰 (API 호출 시 Authorization 헤더에 사용)"),
                                fieldWithPath("data.refreshToken")
                                        .type(JsonFieldType.STRING)
                                        .description("JWT 리프레시 토큰 (액세스 토큰 갱신 시 사용)"),
                                fieldWithPath("data.memberId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("회원 ID"),
                                fieldWithPath("data.email")
                                        .type(JsonFieldType.STRING)
                                        .description("이메일 주소 (카카오 계정 이메일)"),
                                fieldWithPath("data.name")
                                        .type(JsonFieldType.STRING)
                                        .description("사용자명 (카카오 계정 이름)"),
                                fieldWithPath("data.profileImageUrl")
                                        .type(JsonFieldType.STRING)
                                        .description("프로필 이미지 URL"),
                                fieldWithPath("data.isNewMember")
                                        .type(JsonFieldType.BOOLEAN)
                                        .description("신규 회원 여부 (true: 신규 가입, false: 기존 회원)"),
                                fieldWithPath("error")
                                        .type(JsonFieldType.NULL)
                                        .optional()
                                        .description("에러 정보 (성공 시 null)")
                        )
                ));
    }

    @Test
    @DisplayName("구글 로그인 성공 - 신규 회원")
    void googleLogin_newMember_success_docs() throws Exception {
        // given - 구글 로그인 신규 회원 응답
        GoogleLoginServiceResponse response = GoogleLoginServiceResponse.ofNewMember(
                "google-access-token-jwt",
                "google-refresh-token-jwt",
                2001L,
                "google_user@gmail.com",
                "구글사용자",
                "https://google.com/profile.jpg"
        );

        given(googleLoginService.login(any()))
                .willReturn(response);

        // when
        String requestBody = """
                {
                    "authorizationCode": "google-auth-code-xyz789",
                    "redirectUri": "http://localhost:3000/auth/google/callback"
                }
                """;

        // then - 신규 회원 성공 시나리오 문서화
        mockMvc.perform(post("/api/v1/auth/login/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.isNewMember").value(true))
                .andDo(document("auth/login/google/new-member-success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                                fieldWithPath("authorizationCode")
                                        .type(JsonFieldType.STRING)
                                        .description("구글 OAuth 인가 코드"),
                                fieldWithPath("redirectUri")
                                        .type(JsonFieldType.STRING)
                                        .description("리다이렉트 URI (구글 콘솔 설정과 일치해야 함)")
                        ),
                        responseFields(
                                fieldWithPath("result")
                                        .type(JsonFieldType.STRING)
                                        .description("응답 결과 코드 (SUCCESS)"),
                                fieldWithPath("data")
                                        .type(JsonFieldType.OBJECT)
                                        .description("로그인 결과 데이터"),
                                fieldWithPath("data.accessToken")
                                        .type(JsonFieldType.STRING)
                                        .description("JWT 액세스 토큰 (API 호출 시 Authorization 헤더에 사용)"),
                                fieldWithPath("data.refreshToken")
                                        .type(JsonFieldType.STRING)
                                        .description("JWT 리프레시 토큰 (액세스 토큰 갱신 시 사용)"),
                                fieldWithPath("data.memberId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("회원 ID"),
                                fieldWithPath("data.email")
                                        .type(JsonFieldType.STRING)
                                        .description("이메일 주소 (구글 계정 이메일)"),
                                fieldWithPath("data.name")
                                        .type(JsonFieldType.STRING)
                                        .description("사용자명 (구글 계정 이름)"),
                                fieldWithPath("data.profileImageUrl")
                                        .type(JsonFieldType.STRING)
                                        .description("프로필 이미지 URL"),
                                fieldWithPath("data.isNewMember")
                                        .type(JsonFieldType.BOOLEAN)
                                        .description("신규 회원 여부 (true: 신규 가입, false: 기존 회원)"),
                                fieldWithPath("error")
                                        .type(JsonFieldType.NULL)
                                        .optional()
                                        .description("에러 정보 (성공 시 null)")
                        )
                ));
    }

    @Test
    @DisplayName("구글 로그인 성공 - 기존 회원")
    void googleLogin_existingMember_success_docs() throws Exception {
        // given - 구글 로그인 기존 회원 응답
        GoogleLoginServiceResponse response = GoogleLoginServiceResponse.ofExistingMember(
                "google-access-token-jwt-existing",
                "google-refresh-token-jwt-existing",
                2002L,
                "google_existing@gmail.com",
                "구글기존사용자",
                "https://google.com/profile2.jpg"
        );

        given(googleLoginService.login(any()))
                .willReturn(response);

        // when
        String requestBody = """
                {
                    "authorizationCode": "google-auth-code-uvw123",
                    "redirectUri": "http://localhost:3000/auth/google/callback"
                }
                """;

        // then - 기존 회원 로그인 시나리오 문서화
        mockMvc.perform(post("/api/v1/auth/login/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.isNewMember").value(false))
                .andDo(document("auth/login/google/existing-member-success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                                fieldWithPath("authorizationCode")
                                        .type(JsonFieldType.STRING)
                                        .description("구글 OAuth 인가 코드"),
                                fieldWithPath("redirectUri")
                                        .type(JsonFieldType.STRING)
                                        .description("리다이렉트 URI (구글 콘솔 설정과 일치해야 함)")
                        ),
                        responseFields(
                                fieldWithPath("result")
                                        .type(JsonFieldType.STRING)
                                        .description("응답 결과 코드 (SUCCESS)"),
                                fieldWithPath("data")
                                        .type(JsonFieldType.OBJECT)
                                        .description("로그인 결과 데이터"),
                                fieldWithPath("data.accessToken")
                                        .type(JsonFieldType.STRING)
                                        .description("JWT 액세스 토큰 (API 호출 시 Authorization 헤더에 사용)"),
                                fieldWithPath("data.refreshToken")
                                        .type(JsonFieldType.STRING)
                                        .description("JWT 리프레시 토큰 (액세스 토큰 갱신 시 사용)"),
                                fieldWithPath("data.memberId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("회원 ID"),
                                fieldWithPath("data.email")
                                        .type(JsonFieldType.STRING)
                                        .description("이메일 주소 (구글 계정 이메일)"),
                                fieldWithPath("data.name")
                                        .type(JsonFieldType.STRING)
                                        .description("사용자명 (구글 계정 이름)"),
                                fieldWithPath("data.profileImageUrl")
                                        .type(JsonFieldType.STRING)
                                        .description("프로필 이미지 URL"),
                                fieldWithPath("data.isNewMember")
                                        .type(JsonFieldType.BOOLEAN)
                                        .description("신규 회원 여부 (true: 신규 가입, false: 기존 회원)"),
                                fieldWithPath("error")
                                        .type(JsonFieldType.NULL)
                                        .optional()
                                        .description("에러 정보 (성공 시 null)")
                        )
                ));
    }

    @Test
    @DisplayName("카카오 로그인 실패 - 유효하지 않은 인가 코드")
    void kakaoLogin_invalidCode_failure_docs() throws Exception {
        // given - 카카오 로그인 실패 모킹
        given(kakaoLoginService.login(any()))
                .willThrow(new IllegalArgumentException("유효하지 않은 인가 코드"));

        // when
        String requestBody = """
                {
                    "authorizationCode": "invalid-kakao-code",
                    "redirectUri": "http://localhost:3000/auth/kakao/callback"
                }
                """;

        // then - 실패 시나리오 문서화
        mockMvc.perform(post("/api/v1/auth/login/kakao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andDo(document("auth/login/kakao/invalid-code-failure",
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
                                        .description("응답 결과 코드 (ERROR)"),
                                fieldWithPath("data")
                                        .type(JsonFieldType.NULL)
                                        .description("응답 데이터 (실패 시 null)"),
                                fieldWithPath("error")
                                        .type(JsonFieldType.OBJECT)
                                        .description("에러 정보"),
                                fieldWithPath("error.code")
                                        .type(JsonFieldType.STRING)
                                        .description("에러 코드 (E401: 인증 실패)"),
                                fieldWithPath("error.message")
                                        .type(JsonFieldType.STRING)
                                        .description("에러 메시지")
                        )
                ));
    }

    @Test
    @DisplayName("구글 로그인 실패 - 유효하지 않은 인가 코드")
    void googleLogin_invalidCode_failure_docs() throws Exception {
        // given - 구글 로그인 실패 모킹
        given(googleLoginService.login(any()))
                .willThrow(new IllegalArgumentException("유효하지 않은 인가 코드"));

        // when
        String requestBody = """
                {
                    "authorizationCode": "invalid-google-code",
                    "redirectUri": "http://localhost:3000/auth/google/callback"
                }
                """;

        // then - 실패 시나리오 문서화
        mockMvc.perform(post("/api/v1/auth/login/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andDo(document("auth/login/google/invalid-code-failure",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                                fieldWithPath("authorizationCode")
                                        .type(JsonFieldType.STRING)
                                        .description("유효하지 않은 구글 인가 코드"),
                                fieldWithPath("redirectUri")
                                        .type(JsonFieldType.STRING)
                                        .description("리다이렉트 URI")
                        ),
                        responseFields(
                                fieldWithPath("result")
                                        .type(JsonFieldType.STRING)
                                        .description("응답 결과 코드 (ERROR)"),
                                fieldWithPath("data")
                                        .type(JsonFieldType.NULL)
                                        .description("응답 데이터 (실패 시 null)"),
                                fieldWithPath("error")
                                        .type(JsonFieldType.OBJECT)
                                        .description("에러 정보"),
                                fieldWithPath("error.code")
                                        .type(JsonFieldType.STRING)
                                        .description("에러 코드 (E401: 인증 실패)"),
                                fieldWithPath("error.message")
                                        .type(JsonFieldType.STRING)
                                        .description("에러 메시지")
                        )
                ));
    }

    @Test
    @DisplayName("카카오 로그인 실패 - 요청 필드 검증 실패")
    void kakaoLogin_validation_failure_docs() throws Exception {
        // given - 요청 필드 검증 실패
        String requestBody = """
                {
                    "authorizationCode": "",
                    "redirectUri": ""
                }
                """;

        // then - 검증 실패 시나리오 문서화
        mockMvc.perform(post("/api/v1/auth/login/kakao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andDo(document("auth/login/kakao/validation-failure",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                                fieldWithPath("authorizationCode")
                                        .type(JsonFieldType.STRING)
                                        .description("카카오 인가 코드 (빈 문자열 - 검증 실패)"),
                                fieldWithPath("redirectUri")
                                        .type(JsonFieldType.STRING)
                                        .description("리다이렉트 URI (빈 문자열 - 검증 실패)")
                        ),
                        responseFields(
                                fieldWithPath("result")
                                        .type(JsonFieldType.STRING)
                                        .description("응답 결과 코드 (ERROR)"),
                                fieldWithPath("data")
                                        .type(JsonFieldType.NULL)
                                        .description("응답 데이터 (실패 시 null)"),
                                fieldWithPath("error")
                                        .type(JsonFieldType.OBJECT)
                                        .description("에러 정보"),
                                fieldWithPath("error.code")
                                        .type(JsonFieldType.STRING)
                                        .description("에러 코드 (E422: 요청 검증 실패)"),
                                fieldWithPath("error.message")
                                        .type(JsonFieldType.STRING)
                                        .description("에러 메시지")
                        )
                ));
    }

    @Test
    @DisplayName("구글 로그인 실패 - 요청 필드 검증 실패")
    void googleLogin_validation_failure_docs() throws Exception {
        // given - 요청 필드 검증 실패
        String requestBody = """
                {
                    "authorizationCode": "",
                    "redirectUri": ""
                }
                """;

        // then - 검증 실패 시나리오 문서화
        mockMvc.perform(post("/api/v1/auth/login/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andDo(document("auth/login/google/validation-failure",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                                fieldWithPath("authorizationCode")
                                        .type(JsonFieldType.STRING)
                                        .description("구글 인가 코드 (빈 문자열 - 검증 실패)"),
                                fieldWithPath("redirectUri")
                                        .type(JsonFieldType.STRING)
                                        .description("리다이렉트 URI (빈 문자열 - 검증 실패)")
                        ),
                        responseFields(
                                fieldWithPath("result")
                                        .type(JsonFieldType.STRING)
                                        .description("응답 결과 코드 (ERROR)"),
                                fieldWithPath("data")
                                        .type(JsonFieldType.NULL)
                                        .description("응답 데이터 (실패 시 null)"),
                                fieldWithPath("error")
                                        .type(JsonFieldType.OBJECT)
                                        .description("에러 정보"),
                                fieldWithPath("error.code")
                                        .type(JsonFieldType.STRING)
                                        .description("에러 코드 (E422: 요청 검증 실패)"),
                                fieldWithPath("error.message")
                                        .type(JsonFieldType.STRING)
                                        .description("에러 메시지")
                        )
                ));
    }
}

package com.stdev.smartmealtable.api.member.controller;

import com.stdev.smartmealtable.domain.common.vo.Address;
import com.stdev.smartmealtable.domain.common.vo.AddressType;
import com.stdev.smartmealtable.api.auth.service.SignupService;
import com.stdev.smartmealtable.api.auth.service.dto.SignupServiceRequest;
import com.stdev.smartmealtable.api.common.AbstractRestDocsTest;
import com.stdev.smartmealtable.domain.member.entity.MemberAuthentication;
import com.stdev.smartmealtable.domain.member.repository.MemberAuthenticationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 비밀번호 만료 관리 API REST Docs 테스트
 */
@DisplayName("PasswordExpiryController REST Docs 테스트")
@Transactional
class PasswordExpiryControllerRestDocsTest extends AbstractRestDocsTest {

    @Autowired
    private SignupService signupService;

    @Autowired
    private MemberAuthenticationRepository memberAuthenticationRepository;

    private Long testMemberId;
    private String testEmail = "password@example.com";
    private String testPassword = "Test@1234";

    @BeforeEach
    void setUp() {
        // 테스트용 회원 생성
        signupService.signup(new SignupServiceRequest(
                "비밀번호테스트",
                testEmail,
                testPassword
        ));

        MemberAuthentication auth = memberAuthenticationRepository.findByEmail(testEmail)
                .orElseThrow();
        testMemberId = auth.getMemberId();
    }

    /**
     * Authorization 헤더 생성 헬퍼 메서드
     */
    private String createAuthorizationHeader(Long memberId) {
        String token = jwtTokenProvider.createToken(memberId);
        return "Bearer " + token;
    }

    @Test
    @DisplayName("비밀번호 만료 상태 조회 성공")
    void getPasswordExpiryStatus_success_docs() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/members/me/password/expiry-status")
                        .header("Authorization", createAuthorizationHeader(testMemberId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data").exists())
                .andDo(document("password-expiry/get-status-success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(
                                headerWithName("Authorization")
                                        .description("JWT 액세스 토큰 (Bearer 스킴). 인증된 사용자의 memberId를 포함")
                        ),
                        responseFields(
                                fieldWithPath("result")
                                        .type(JsonFieldType.STRING)
                                        .description("응답 결과 (SUCCESS)"),
                                fieldWithPath("data")
                                        .type(JsonFieldType.OBJECT)
                                        .description("응답 데이터"),
                                fieldWithPath("data.passwordChangedAt")
                                        .type(JsonFieldType.STRING)
                                        .description("비밀번호 마지막 변경 일시 (ISO-8601 형식)"),
                                fieldWithPath("data.passwordExpiresAt")
                                        .type(JsonFieldType.STRING)
                                        .description("비밀번호 만료 일시 (ISO-8601 형식)")
                                        .optional(),
                                fieldWithPath("data.daysRemaining")
                                        .type(JsonFieldType.NUMBER)
                                        .description("만료까지 남은 일수 (음수면 이미 만료, null이면 만료 없음)")
                                        .optional(),
                                fieldWithPath("data.isExpired")
                                        .type(JsonFieldType.BOOLEAN)
                                        .description("비밀번호 만료 여부"),
                                fieldWithPath("data.isExpiringSoon")
                                        .type(JsonFieldType.BOOLEAN)
                                        .description("만료 임박 여부 (7일 이내)"),
                                fieldWithPath("error")
                                        .type(JsonFieldType.NULL)
                                        .optional()
                                        .description("에러 정보 (성공 시 null)")
                        )
                ));
    }

    @Test
    @DisplayName("비밀번호 만료일 연장 성공")
    void extendPasswordExpiry_success_docs() throws Exception {
        // when & then
        mockMvc.perform(post("/api/v1/members/me/password/extend-expiry")
                        .header("Authorization", createAuthorizationHeader(testMemberId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data").exists())
                .andDo(document("password-expiry/extend-success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(
                                headerWithName("Authorization")
                                        .description("JWT 액세스 토큰 (Bearer 스킴). 인증된 사용자의 memberId를 포함")
                        ),
                        responseFields(
                                fieldWithPath("result")
                                        .type(JsonFieldType.STRING)
                                        .description("응답 결과 (SUCCESS)"),
                                fieldWithPath("data")
                                        .type(JsonFieldType.OBJECT)
                                        .description("응답 데이터"),
                                fieldWithPath("data.newExpiresAt")
                                        .type(JsonFieldType.STRING)
                                        .description("연장된 비밀번호 만료 일시 (ISO-8601 형식)"),
                                fieldWithPath("data.message")
                                        .type(JsonFieldType.STRING)
                                        .description("연장 완료 메시지"),
                                fieldWithPath("error")
                                        .type(JsonFieldType.NULL)
                                        .optional()
                                        .description("에러 정보 (성공 시 null)")
                        )
                ));
    }

    @Test
    @DisplayName("비밀번호 만료 상태 조회 실패 - 회원 없음")
    void getPasswordExpiryStatus_notFound_docs() throws Exception {
        // given
        Long nonExistentId = 999999L;

        // when & then
        mockMvc.perform(get("/api/v1/members/me/password/expiry-status")
                        .header("Authorization", createAuthorizationHeader(nonExistentId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E404"))
                .andDo(document("password-expiry/get-status-not-found",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(
                                headerWithName("Authorization")
                                        .description("JWT 액세스 토큰 (존재하지 않는 회원 ID 포함)")
                        ),
                        responseFields(
                                fieldWithPath("result")
                                        .type(JsonFieldType.STRING)
                                        .description("응답 결과 (ERROR)"),
                                fieldWithPath("data")
                                        .type(JsonFieldType.NULL)
                                        .optional()
                                        .description("응답 데이터 (에러 시 null)"),
                                fieldWithPath("error")
                                        .type(JsonFieldType.OBJECT)
                                        .description("에러 정보"),
                                fieldWithPath("error.code")
                                        .type(JsonFieldType.STRING)
                                        .description("에러 코드 (E404)"),
                                fieldWithPath("error.message")
                                        .type(JsonFieldType.STRING)
                                        .description("에러 메시지"),
                                fieldWithPath("error.data")
                                        .type(JsonFieldType.NULL)
                                        .optional()
                                        .description("에러 상세 데이터")
                        )
                ));
    }

    @Test
    @DisplayName("비밀번호 만료일 연장 실패 - 회원 없음")
    void extendPasswordExpiry_notFound_docs() throws Exception {
        // given
        Long nonExistentId = 999999L;

        // when & then
        mockMvc.perform(post("/api/v1/members/me/password/extend-expiry")
                        .header("Authorization", createAuthorizationHeader(nonExistentId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E404"))
                .andDo(document("password-expiry/extend-not-found",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(
                                headerWithName("Authorization")
                                        .description("JWT 액세스 토큰 (존재하지 않는 회원 ID 포함)")
                        ),
                        responseFields(
                                fieldWithPath("result")
                                        .type(JsonFieldType.STRING)
                                        .description("응답 결과 (ERROR)"),
                                fieldWithPath("data")
                                        .type(JsonFieldType.NULL)
                                        .optional()
                                        .description("응답 데이터 (에러 시 null)"),
                                fieldWithPath("error")
                                        .type(JsonFieldType.OBJECT)
                                        .description("에러 정보"),
                                fieldWithPath("error.code")
                                        .type(JsonFieldType.STRING)
                                        .description("에러 코드 (E404)"),
                                fieldWithPath("error.message")
                                        .type(JsonFieldType.STRING)
                                        .description("에러 메시지"),
                                fieldWithPath("error.data")
                                        .type(JsonFieldType.NULL)
                                        .optional()
                                        .description("에러 상세 데이터")
                        )
                ));
    }
}

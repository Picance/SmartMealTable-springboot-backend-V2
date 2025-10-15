package com.stdev.smartmealtable.api.auth.controller;

import com.stdev.smartmealtable.api.auth.service.SignupService;
import com.stdev.smartmealtable.api.auth.service.dto.SignupServiceRequest;
import com.stdev.smartmealtable.api.common.AbstractRestDocsTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 이메일 로그인 API Rest Docs 테스트
 */
@DisplayName("이메일 로그인 API Rest Docs")
@Transactional
class LoginControllerRestDocsTest extends AbstractRestDocsTest {

    @Autowired
    private SignupService signupService;

    private String testEmail = "login@example.com";
    private String testPassword = "Test@1234";

    @BeforeEach
    void setUp() {
        // 테스트용 회원 생성
        signupService.signup(new SignupServiceRequest(
                "테스트사용자",
                testEmail,
                testPassword
        ));
    }

    @Test
    @DisplayName("이메일 로그인 성공 API 문서화")
    void login_success_docs() throws Exception {
        // given
        Map<String, String> request = new HashMap<>();
        request.put("email", testEmail);
        request.put("password", testPassword);

        // when & then
        mockMvc.perform(post("/api/v1/auth/login/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andDo(document("auth/login/email/success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                                fieldWithPath("email")
                                        .type(JsonFieldType.STRING)
                                        .description("로그인 이메일 주소"),
                                fieldWithPath("password")
                                        .type(JsonFieldType.STRING)
                                        .description("비밀번호")
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
                                        .description("액세스 토큰 (유효기간: 1시간)"),
                                fieldWithPath("data.refreshToken")
                                        .type(JsonFieldType.STRING)
                                        .description("리프레시 토큰 (유효기간: 24시간)"),
                                fieldWithPath("data.memberId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("회원 ID"),
                                fieldWithPath("data.email")
                                        .type(JsonFieldType.STRING)
                                        .description("회원 이메일"),
                                fieldWithPath("data.name")
                                        .type(JsonFieldType.STRING)
                                        .description("회원 이름"),
                                fieldWithPath("data.onboardingComplete")
                                        .type(JsonFieldType.BOOLEAN)
                                        .description("온보딩 완료 여부"),
                                fieldWithPath("error")
                                        .type(JsonFieldType.NULL)
                                        .description("에러 정보 (성공 시 null)")
                                        .optional()
                        )
                ));
    }

    @Test
    @DisplayName("이메일 로그인 실패 - 잘못된 이메일 문서화")
    void login_invalidEmail_docs() throws Exception {
        // given
        Map<String, String> request = new HashMap<>();
        request.put("email", "nonexistent@example.com");
        request.put("password", "SomePassword123!");

        // when & then
        mockMvc.perform(post("/api/v1/auth/login/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andDo(document("auth/login/email/invalid-email",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                                fieldWithPath("email")
                                        .type(JsonFieldType.STRING)
                                        .description("존재하지 않는 이메일"),
                                fieldWithPath("password")
                                        .type(JsonFieldType.STRING)
                                        .description("비밀번호")
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
    @DisplayName("이메일 로그인 실패 - 잘못된 비밀번호 문서화")
    void login_invalidPassword_docs() throws Exception {
        // given
        Map<String, String> request = new HashMap<>();
        request.put("email", testEmail);
        request.put("password", "WrongPassword123!");

        // when & then
        mockMvc.perform(post("/api/v1/auth/login/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andDo(document("auth/login/email/invalid-password",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                                fieldWithPath("email")
                                        .type(JsonFieldType.STRING)
                                        .description("로그인 이메일"),
                                fieldWithPath("password")
                                        .type(JsonFieldType.STRING)
                                        .description("잘못된 비밀번호")
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
    @DisplayName("이메일 로그인 실패 - 유효성 검증 실패 문서화")
    void login_validation_docs() throws Exception {
        // given
        Map<String, String> request = new HashMap<>();
        request.put("email", "");  // 빈 이메일
        request.put("password", testPassword);

        // when & then
        mockMvc.perform(post("/api/v1/auth/login/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andDo(document("auth/login/email/validation-error",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                                fieldWithPath("email")
                                        .type(JsonFieldType.STRING)
                                        .description("빈 이메일 (유효성 검증 실패)"),
                                fieldWithPath("password")
                                        .type(JsonFieldType.STRING)
                                        .description("비밀번호")
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

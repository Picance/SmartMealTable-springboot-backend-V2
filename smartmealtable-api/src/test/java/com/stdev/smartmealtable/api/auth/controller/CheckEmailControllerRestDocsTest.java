package com.stdev.smartmealtable.api.auth.controller;

import com.stdev.smartmealtable.api.auth.service.SignupService;
import com.stdev.smartmealtable.api.auth.service.dto.SignupServiceRequest;
import com.stdev.smartmealtable.api.common.AbstractRestDocsTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 이메일 중복 검증 API Rest Docs 테스트
 */
@DisplayName("이메일 중복 검증 API Rest Docs")
@Transactional
class CheckEmailControllerRestDocsTest extends AbstractRestDocsTest {

    @Autowired
    private SignupService signupService;

    private String existingEmail = "existing@example.com";

    @BeforeEach
    void setUp() {
        // 테스트용 회원 생성 (이메일 중복 테스트용)
        signupService.signup(new SignupServiceRequest(
                "기존사용자",
                existingEmail,
                "Test@1234"
        ));
    }

    @Test
    @DisplayName("이메일 중복 검증 - 사용 가능한 이메일 문서화")
    void checkEmail_available_docs() throws Exception {
        // given
        String availableEmail = "available@example.com";

        // when & then
        mockMvc.perform(get("/api/v1/auth/check-email")
                        .param("email", availableEmail))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andDo(document("auth/check-email/available",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        queryParameters(
                                parameterWithName("email")
                                        .description("검증할 이메일 주소")
                        ),
                        responseFields(
                                fieldWithPath("result")
                                        .type(JsonFieldType.STRING)
                                        .description("결과 상태 (SUCCESS)"),
                                fieldWithPath("data")
                                        .type(JsonFieldType.OBJECT)
                                        .description("응답 데이터"),
                                fieldWithPath("data.available")
                                        .type(JsonFieldType.BOOLEAN)
                                        .description("사용 가능 여부 (true: 사용 가능, false: 중복)"),
                                fieldWithPath("data.message")
                                        .type(JsonFieldType.STRING)
                                        .description("결과 메시지")
                        )
                ));
    }

    @Test
    @DisplayName("이메일 중복 검증 - 이미 사용 중인 이메일 문서화")
    void checkEmail_duplicate_docs() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/auth/check-email")
                        .param("email", existingEmail))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andDo(document("auth/check-email/duplicate",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        queryParameters(
                                parameterWithName("email")
                                        .description("검증할 이메일 주소 (이미 사용 중)")
                        ),
                        responseFields(
                                fieldWithPath("result")
                                        .type(JsonFieldType.STRING)
                                        .description("결과 상태 (SUCCESS)"),
                                fieldWithPath("data")
                                        .type(JsonFieldType.OBJECT)
                                        .description("응답 데이터"),
                                fieldWithPath("data.available")
                                        .type(JsonFieldType.BOOLEAN)
                                        .description("사용 가능 여부 (false: 이미 사용 중)"),
                                fieldWithPath("data.message")
                                        .type(JsonFieldType.STRING)
                                        .description("결과 메시지")
                        )
                ));
    }

    @Test
    @DisplayName("이메일 중복 검증 - 유효하지 않은 이메일 형식 문서화")
    void checkEmail_invalidFormat_docs() throws Exception {
        // given
        String invalidEmail = "invalid-email-format";

        // when & then
        mockMvc.perform(get("/api/v1/auth/check-email")
                        .param("email", invalidEmail))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andDo(document("auth/check-email/invalid-format",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        queryParameters(
                                parameterWithName("email")
                                        .description("유효하지 않은 이메일 형식")
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
                                        .type(JsonFieldType.NULL)
                                        .description("에러 상세 데이터")
                                        .optional()
                        )
                ));
    }
}

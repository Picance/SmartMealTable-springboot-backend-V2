package com.stdev.smartmealtable.api.auth.controller;

import com.stdev.smartmealtable.api.common.AbstractRestDocsTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 회원가입 API Rest Docs 테스트
 */
@DisplayName("회원가입 API Rest Docs")
class SignupControllerRestDocsTest extends AbstractRestDocsTest {

    @Test
    @DisplayName("이메일 회원가입 API 문서화")
    void signup_docs() throws Exception {
        // given
        Map<String, String> request = new HashMap<>();
        request.put("name", "홍길동");
        request.put("email", "hong@example.com");
        request.put("password", "SecureP@ss123!");

        // when & then
        mockMvc.perform(post("/api/v1/auth/signup/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andDo(document("auth/signup/email/success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                                fieldWithPath("name")
                                        .type(JsonFieldType.STRING)
                                        .description("사용자 이름 (2-50자)"),
                                fieldWithPath("email")
                                        .type(JsonFieldType.STRING)
                                        .description("이메일 주소 (이메일 형식)"),
                                fieldWithPath("password")
                                        .type(JsonFieldType.STRING)
                                        .description("비밀번호 (8-20자, 영문+숫자+특수문자)")
                        ),
                        responseFields(
                                fieldWithPath("result")
                                        .type(JsonFieldType.STRING)
                                        .description("결과 상태 (SUCCESS/ERROR)"),
                                fieldWithPath("data")
                                        .type(JsonFieldType.OBJECT)
                                        .description("응답 데이터"),
                                fieldWithPath("data.memberId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("생성된 회원 ID"),
                                fieldWithPath("data.email")
                                        .type(JsonFieldType.STRING)
                                        .description("회원 이메일"),
                                fieldWithPath("data.name")
                                        .type(JsonFieldType.STRING)
                                        .description("회원 이름"),
                                fieldWithPath("error")
                                        .type(JsonFieldType.NULL)
                                        .description("에러 정보 (성공 시 null)")
                                        .optional()
                        )
                ));
    }

    @Test
    @DisplayName("이메일 중복 에러 문서화")
    void signup_duplicateEmail_docs() throws Exception {
        // given - 먼저 회원가입
        Map<String, String> firstRequest = new HashMap<>();
        firstRequest.put("name", "홍길동");
        firstRequest.put("email", "duplicate@example.com");
        firstRequest.put("password", "SecureP@ss123!");

        mockMvc.perform(post("/api/v1/auth/signup/email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequest)));

        // when - 같은 이메일로 다시 가입 시도
        Map<String, String> secondRequest = new HashMap<>();
        secondRequest.put("name", "김철수");
        secondRequest.put("email", "duplicate@example.com");
        secondRequest.put("password", "AnotherP@ss456!");

        // then
        mockMvc.perform(post("/api/v1/auth/signup/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondRequest)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andDo(document("auth/signup/email/duplicate-email",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                                fieldWithPath("name")
                                        .type(JsonFieldType.STRING)
                                        .description("사용자 이름"),
                                fieldWithPath("email")
                                        .type(JsonFieldType.STRING)
                                        .description("이메일 주소 (중복된 이메일)"),
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
                                        .description("에러 코드 (E409)"),
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
    @DisplayName("유효성 검증 실패 문서화")
    void signup_validation_docs() throws Exception {
        // given
        Map<String, String> request = new HashMap<>();
        request.put("name", "홍길동");
        request.put("email", "invalid-email-format");  // 잘못된 이메일 형식
        request.put("password", "SecureP@ss123!");

        // when & then
        mockMvc.perform(post("/api/v1/auth/signup/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andDo(document("auth/signup/email/validation-error",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                                fieldWithPath("name")
                                        .type(JsonFieldType.STRING)
                                        .description("사용자 이름"),
                                fieldWithPath("email")
                                        .type(JsonFieldType.STRING)
                                        .description("이메일 주소 (잘못된 형식)"),
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

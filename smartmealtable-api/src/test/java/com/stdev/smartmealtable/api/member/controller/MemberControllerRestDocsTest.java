package com.stdev.smartmealtable.api.member.controller;

import com.stdev.smartmealtable.api.auth.service.SignupService;
import com.stdev.smartmealtable.api.auth.service.dto.SignupServiceRequest;
import com.stdev.smartmealtable.api.common.AbstractRestDocsTest;
import com.stdev.smartmealtable.domain.member.entity.Group;
import com.stdev.smartmealtable.domain.member.entity.GroupType;
import com.stdev.smartmealtable.domain.member.entity.MemberAuthentication;
import com.stdev.smartmealtable.domain.member.repository.GroupRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberAuthenticationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 회원 관리 API Rest Docs 테스트
 */
@DisplayName("회원 관리 API Rest Docs")
@Transactional
class MemberControllerRestDocsTest extends AbstractRestDocsTest {

    @Autowired
    private SignupService signupService;

    @Autowired
    private MemberAuthenticationRepository memberAuthenticationRepository;

    @Autowired
    private GroupRepository groupRepository;

    private Long testMemberId;
    private Long testGroupId;
    private String testEmail = "member@example.com";
    private String testPassword = "Test@1234";
    private String accessToken;

    @BeforeEach
    void setUp() {
        // 테스트용 그룹 생성
        Group testGroup = Group.create("학생", GroupType.UNIVERSITY, "대학생 그룹");
        Group savedGroup = groupRepository.save(testGroup);
        testGroupId = savedGroup.getGroupId();

        // 테스트용 회원 생성
        signupService.signup(new SignupServiceRequest(
                "테스트사용자",
                testEmail,
                testPassword
        ));

        MemberAuthentication auth = memberAuthenticationRepository.findByEmail(testEmail)
                .orElseThrow();
        testMemberId = auth.getMemberId();
        
        // JWT Access Token 생성
        accessToken = createAccessToken(testMemberId);
    }

    @Test
    @DisplayName("내 프로필 조회 성공 API 문서화")
    void getMyProfile_success_docs() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/members/me")
                        .header("Authorization", accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andDo(document("members/get-profile/success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(
                                headerWithName("Authorization")
                                        .description("JWT 인증 토큰 (Bearer {token})")
                        ),
                        responseFields(
                                fieldWithPath("result")
                                        .type(JsonFieldType.STRING)
                                        .description("결과 상태 (SUCCESS)"),
                                fieldWithPath("data")
                                        .type(JsonFieldType.OBJECT)
                                        .description("응답 데이터"),
                                fieldWithPath("data.memberId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("회원 ID"),
                                fieldWithPath("data.email")
                                        .type(JsonFieldType.STRING)
                                        .description("회원 이메일"),
                                fieldWithPath("data.name")
                                        .type(JsonFieldType.STRING)
                                        .description("회원 이름"),
                                fieldWithPath("data.nickname")
                                        .type(JsonFieldType.STRING)
                                        .description("닉네임")
                                        .optional(),
                                fieldWithPath("data.recommendationType")
                                        .type(JsonFieldType.STRING)
                                        .description("추천 알고리즘 타입 (BALANCED, ADVENTURE, STABLE)"),
                                fieldWithPath("data.group")
                                        .type(JsonFieldType.OBJECT)
                                        .description("소속 그룹 정보")
                                        .optional(),
                                fieldWithPath("data.group.groupId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("그룹 ID")
                                        .optional(),
                                fieldWithPath("data.group.name")
                                        .type(JsonFieldType.STRING)
                                        .description("그룹 이름")
                                        .optional(),
                                fieldWithPath("data.group.type")
                                        .type(JsonFieldType.STRING)
                                        .description("그룹 타입 (UNIVERSITY, COMPANY, OTHER)")
                                        .optional(),
                                fieldWithPath("data.socialAccounts")
                                        .type(JsonFieldType.ARRAY)
                                        .description("연결된 소셜 계정 목록"),
                                fieldWithPath("data.passwordExpiresAt")
                                        .type(JsonFieldType.STRING)
                                        .description("비밀번호 만료일"),
                                fieldWithPath("data.createdAt")
                                        .type(JsonFieldType.STRING)
                                        .description("회원 가입일"),
                                fieldWithPath("error")
                                        .type(JsonFieldType.NULL)
                                        .description("에러 정보 (성공 시 null)")
                                        .optional()
                        )
                ));
    }

    @Test
    @DisplayName("내 프로필 조회 실패 - 존재하지 않는 회원 (404) API 문서화")
    void getMyProfile_notFound_docs() throws Exception {
        // given
        Long nonExistentMemberId = 99999L;
        String invalidAccessToken = createAccessToken(nonExistentMemberId);

        // when & then
        mockMvc.perform(get("/api/v1/members/me")
                        .header("Authorization", invalidAccessToken))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andDo(document("members/get-profile/not-found",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(
                                headerWithName("Authorization")
                                        .description("JWT 인증 토큰 (존재하지 않는 회원의 토큰)")
                        ),
                        responseFields(
                                fieldWithPath("result")
                                        .type(JsonFieldType.STRING)
                                        .description("결과 상태 (ERROR)"),
                                fieldWithPath("error")
                                        .type(JsonFieldType.OBJECT)
                                        .description("에러 정보"),
                                fieldWithPath("error.code")
                                        .type(JsonFieldType.STRING)
                                        .description("에러 코드"),
                                fieldWithPath("error.message")
                                        .type(JsonFieldType.STRING)
                                        .description("에러 메시지")
                        )
                ));
    }

    @Test
    @DisplayName("프로필 수정 성공 API 문서화")
    void updateProfile_success_docs() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>();
        request.put("nickname", "새닉네임");
        request.put("groupId", testGroupId);

        // when & then
        mockMvc.perform(put("/api/v1/members/me")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andDo(document("members/update-profile/success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(
                                headerWithName("Authorization")
                                        .description("회원 ID (JWT 토큰에서 추출)")
                        ),
                        requestFields(
                                fieldWithPath("nickname")
                                        .type(JsonFieldType.STRING)
                                        .description("변경할 닉네임 (2-20자)")
                                        .optional(),
                                fieldWithPath("groupId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("변경할 그룹 ID")
                                        .optional()
                        ),
                        responseFields(
                                fieldWithPath("result")
                                        .type(JsonFieldType.STRING)
                                        .description("결과 상태 (SUCCESS)"),
                                fieldWithPath("data")
                                        .type(JsonFieldType.OBJECT)
                                        .description("응답 데이터"),
                                fieldWithPath("data.memberId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("회원 ID"),
                                fieldWithPath("data.nickname")
                                        .type(JsonFieldType.STRING)
                                        .description("변경된 닉네임")
                                        .optional(),
                                fieldWithPath("data.group")
                                        .type(JsonFieldType.OBJECT)
                                        .description("변경된 그룹 정보")
                                        .optional(),
                                fieldWithPath("data.group.groupId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("그룹 ID")
                                        .optional(),
                                fieldWithPath("data.group.name")
                                        .type(JsonFieldType.STRING)
                                        .description("그룹 이름")
                                        .optional(),
                                fieldWithPath("data.group.type")
                                        .type(JsonFieldType.STRING)
                                        .description("그룹 타입")
                                        .optional(),
                                fieldWithPath("data.updatedAt")
                                        .type(JsonFieldType.STRING)
                                        .description("수정일시"),
                                fieldWithPath("error")
                                        .type(JsonFieldType.NULL)
                                        .description("에러 정보 (성공 시 null)")
                                        .optional()
                        )
                ));
    }

    @Test
    @DisplayName("프로필 수정 실패 - 유효하지 않은 닉네임 (422) API 문서화")
    void updateProfile_invalidNickname_docs() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>();
        request.put("nickname", "a"); // 너무 짧은 닉네임

        // when & then
        mockMvc.perform(put("/api/v1/members/me")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andDo(document("members/update-profile/invalid-nickname",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(
                                headerWithName("Authorization")
                                        .description("회원 ID")
                        ),
                        requestFields(
                                fieldWithPath("nickname")
                                        .type(JsonFieldType.STRING)
                                        .description("유효하지 않은 닉네임 (1자)")
                        ),
                        responseFields(
                                fieldWithPath("result")
                                        .type(JsonFieldType.STRING)
                                        .description("결과 상태 (ERROR)"),
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

    @Test
    @DisplayName("비밀번호 변경 성공 API 문서화")
    void changePassword_success_docs() throws Exception {
        // given
        Map<String, String> request = new HashMap<>();
        request.put("currentPassword", testPassword);
        request.put("newPassword", "NewTest@1234");

        // when & then
        mockMvc.perform(put("/api/v1/members/me/password")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andDo(document("members/change-password/success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(
                                headerWithName("Authorization")
                                        .description("회원 ID (JWT 토큰에서 추출)")
                        ),
                        requestFields(
                                fieldWithPath("currentPassword")
                                        .type(JsonFieldType.STRING)
                                        .description("현재 비밀번호"),
                                fieldWithPath("newPassword")
                                        .type(JsonFieldType.STRING)
                                        .description("새 비밀번호 (8-20자, 영문 대소문자, 숫자, 특수문자 포함)")
                        ),
                        responseFields(
                                fieldWithPath("result")
                                        .type(JsonFieldType.STRING)
                                        .description("결과 상태 (SUCCESS)"),
                                fieldWithPath("data")
                                        .type(JsonFieldType.OBJECT)
                                        .description("응답 데이터"),
                                fieldWithPath("data.message")
                                        .type(JsonFieldType.STRING)
                                        .description("결과 메시지"),
                                fieldWithPath("error")
                                        .type(JsonFieldType.NULL)
                                        .description("에러 정보 (성공 시 null)")
                                        .optional()
                        )
                ));
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 잘못된 현재 비밀번호 (401) API 문서화")
    void changePassword_wrongCurrentPassword_docs() throws Exception {
        // given
        Map<String, String> request = new HashMap<>();
        request.put("currentPassword", "WrongPassword@123");
        request.put("newPassword", "NewTest@1234");

        // when & then
        mockMvc.perform(put("/api/v1/members/me/password")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andDo(document("members/change-password/wrong-current-password",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(
                                headerWithName("Authorization")
                                        .description("회원 ID")
                        ),
                        requestFields(
                                fieldWithPath("currentPassword")
                                        .type(JsonFieldType.STRING)
                                        .description("잘못된 현재 비밀번호"),
                                fieldWithPath("newPassword")
                                        .type(JsonFieldType.STRING)
                                        .description("새 비밀번호")
                        ),
                        responseFields(
                                fieldWithPath("result")
                                        .type(JsonFieldType.STRING)
                                        .description("결과 상태 (ERROR)"),
                                fieldWithPath("error")
                                        .type(JsonFieldType.OBJECT)
                                        .description("에러 정보"),
                                fieldWithPath("error.code")
                                        .type(JsonFieldType.STRING)
                                        .description("에러 코드 (E401)"),
                                fieldWithPath("error.message")
                                        .type(JsonFieldType.STRING)
                                        .description("에러 메시지")
                        )
                ));
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 유효하지 않은 새 비밀번호 (422) API 문서화")
    void changePassword_invalidNewPassword_docs() throws Exception {
        // given
        Map<String, String> request = new HashMap<>();
        request.put("currentPassword", testPassword);
        request.put("newPassword", "weak"); // 약한 비밀번호

        // when & then
        mockMvc.perform(put("/api/v1/members/me/password")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andDo(document("members/change-password/invalid-new-password",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(
                                headerWithName("Authorization")
                                        .description("회원 ID")
                        ),
                        requestFields(
                                fieldWithPath("currentPassword")
                                        .type(JsonFieldType.STRING)
                                        .description("현재 비밀번호"),
                                fieldWithPath("newPassword")
                                        .type(JsonFieldType.STRING)
                                        .description("유효하지 않은 새 비밀번호")
                        ),
                        responseFields(
                                fieldWithPath("result")
                                        .type(JsonFieldType.STRING)
                                        .description("결과 상태 (ERROR)"),
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

    @Test
    @DisplayName("회원 탈퇴 성공 API 문서화")
    void withdrawMember_success_docs() throws Exception {
        // given
        Map<String, String> request = new HashMap<>();
        request.put("password", testPassword);
        request.put("reason", "서비스 이용이 불편해서");

        // when & then
        mockMvc.perform(delete("/api/v1/members/me")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(document("members/withdraw/success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(
                                headerWithName("Authorization")
                                        .description("회원 ID (JWT 토큰에서 추출)")
                        ),
                        requestFields(
                                fieldWithPath("password")
                                        .type(JsonFieldType.STRING)
                                        .description("비밀번호 확인"),
                                fieldWithPath("reason")
                                        .type(JsonFieldType.STRING)
                                        .description("탈퇴 사유")
                                        .optional()
                        )
                ));
    }

    @Test
    @DisplayName("회원 탈퇴 실패 - 잘못된 비밀번호 (401) API 문서화")
    void withdrawMember_wrongPassword_docs() throws Exception {
        // given
        Map<String, String> request = new HashMap<>();
        request.put("password", "WrongPassword@123");

        // when & then
        mockMvc.perform(delete("/api/v1/members/me")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andDo(document("members/withdraw/wrong-password",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(
                                headerWithName("Authorization")
                                        .description("회원 ID")
                        ),
                        requestFields(
                                fieldWithPath("password")
                                        .type(JsonFieldType.STRING)
                                        .description("잘못된 비밀번호")
                        ),
                        responseFields(
                                fieldWithPath("result")
                                        .type(JsonFieldType.STRING)
                                        .description("결과 상태 (ERROR)"),
                                fieldWithPath("error")
                                        .type(JsonFieldType.OBJECT)
                                        .description("에러 정보"),
                                fieldWithPath("error.code")
                                        .type(JsonFieldType.STRING)
                                        .description("에러 코드 (E401)"),
                                fieldWithPath("error.message")
                                        .type(JsonFieldType.STRING)
                                        .description("에러 메시지")
                        )
                ));
    }
}

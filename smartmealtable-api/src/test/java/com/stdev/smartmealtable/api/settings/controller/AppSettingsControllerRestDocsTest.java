package com.stdev.smartmealtable.api.settings.controller;

import com.stdev.smartmealtable.api.common.AbstractRestDocsTest;
import com.stdev.smartmealtable.domain.common.vo.Address;
import com.stdev.smartmealtable.domain.member.entity.Group;
import com.stdev.smartmealtable.domain.member.entity.GroupType;
import com.stdev.smartmealtable.domain.member.entity.Member;
import com.stdev.smartmealtable.domain.member.entity.MemberAuthentication;
import com.stdev.smartmealtable.domain.member.entity.RecommendationType;
import com.stdev.smartmealtable.domain.member.repository.GroupRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberAuthenticationRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberRepository;
import com.stdev.smartmealtable.domain.settings.entity.AppSettings;
import com.stdev.smartmealtable.domain.settings.repository.AppSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 앱 설정 API REST Docs 테스트
 */
@Transactional
@DisplayName("AppSettingsController REST Docs 테스트")
class AppSettingsControllerRestDocsTest extends AbstractRestDocsTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberAuthenticationRepository memberAuthenticationRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private AppSettingsRepository appSettingsRepository;

    private Long testMemberId;

    @BeforeEach
    void setUp() {
        // 테스트용 그룹 생성
        Group testGroup = Group.create("서울대학교", GroupType.UNIVERSITY, Address.of("서울대학교", null, "서울특별시 관악구 관악로 1", null, null, null, null));
        Group savedGroup = groupRepository.save(testGroup);

        // 테스트용 회원 생성
        Member testMember = Member.create(savedGroup.getGroupId(), "테스트닉네임", null, RecommendationType.BALANCED);
        Member savedMember = memberRepository.save(testMember);
        testMemberId = savedMember.getMemberId();

        // 테스트용 인증 정보 생성
        MemberAuthentication auth = MemberAuthentication.createEmailAuth(
                savedMember.getMemberId(),
                "test@example.com",
                "hashedPasswordForTest",
                "테스트유저"
        );
        memberAuthenticationRepository.save(auth);

        // 테스트용 앱 설정 생성
        AppSettings appSettings = AppSettings.create(testMemberId);
        appSettingsRepository.save(appSettings);
    }

    @Test
    @DisplayName("[Docs] 앱 설정 조회 성공")
    void getAppSettings_Success_Docs() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/settings/app"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.privacyPolicyUrl").value("https://smartmealtable.com/policies/privacy"))
                .andExpect(jsonPath("$.data.appVersion").value("1.0.0"))
                .andDo(document("app-settings/get",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("응답 결과 (SUCCESS/ERROR)"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.privacyPolicyUrl").type(JsonFieldType.STRING).description("개인정보처리방침 URL"),
                                fieldWithPath("data.termsOfServiceUrl").type(JsonFieldType.STRING).description("서비스 이용약관 URL"),
                                fieldWithPath("data.contactEmail").type(JsonFieldType.STRING).description("문의 이메일"),
                                fieldWithPath("data.appVersion").type(JsonFieldType.STRING).description("현재 앱 버전"),
                                fieldWithPath("data.minSupportedVersion").type(JsonFieldType.STRING).description("최소 지원 버전"),
                                fieldWithPath("error").type(JsonFieldType.NULL).optional()
                                        .description("에러 정보 (성공 시 null)")
                        )
                ));
    }

    @Test
    @DisplayName("[Docs] 사용자 추적 설정 변경 성공")
    void updateTrackingSettings_Success_Docs() throws Exception {
        // given
        String requestBody = """
                {
                    "allowTracking": true
                }
                """;

        // when & then
        mockMvc.perform(put("/api/v1/settings/app/tracking")
                        .header("Authorization", createAccessToken(testMemberId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.allowTracking").value(true))
                .andDo(document("app-settings/update-tracking",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        requestFields(
                                fieldWithPath("allowTracking").type(JsonFieldType.BOOLEAN).description("사용자 추적 허용 여부")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("응답 결과 (SUCCESS/ERROR)"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.allowTracking").type(JsonFieldType.BOOLEAN).description("사용자 추적 허용 여부"),
                                fieldWithPath("data.updatedAt").type(JsonFieldType.STRING).description("설정 변경 일시"),
                                fieldWithPath("error").type(JsonFieldType.NULL).optional()
                                        .description("에러 정보 (성공 시 null)")
                        )
                ));
    }

    @Test
    @DisplayName("[Docs] 사용자 추적 설정 변경 실패 - 필수 필드 누락")
    void updateTrackingSettings_Fail_MissingField_Docs() throws Exception {
        // given
        String invalidRequest = "{}";

        // when & then
        mockMvc.perform(put("/api/v1/settings/app/tracking")
                        .header("Authorization", createAccessToken(testMemberId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andDo(document("app-settings/update-tracking-fail-missing-field",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("응답 결과 (ERROR)"),
                                fieldWithPath("data").type(JsonFieldType.NULL).optional()
                                        .description("응답 데이터 (에러 시 null)"),
                                fieldWithPath("error").type(JsonFieldType.OBJECT).description("에러 정보"),
                                fieldWithPath("error.code").type(JsonFieldType.STRING).description("에러 코드"),
                                fieldWithPath("error.message").type(JsonFieldType.STRING).description("에러 메시지"),
                                fieldWithPath("error.data").type(JsonFieldType.OBJECT).description("에러 상세 정보"),
                                fieldWithPath("error.data.reason").type(JsonFieldType.STRING).description("에러 사유"),
                                fieldWithPath("error.data.field").type(JsonFieldType.STRING).description("문제가 있는 필드명")
                        )
                ));
    }

    @Test
    @DisplayName("[Docs] 사용자 추적 설정 변경 실패 - 인증 토큰 없음")
    void updateTrackingSettings_Fail_NoAuth_Docs() throws Exception {
        // given
        String requestBody = """
                {
                    "allowTracking": false
                }
                """;

        // when & then
        mockMvc.perform(put("/api/v1/settings/app/tracking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andDo(document("app-settings/update-tracking-fail-no-auth",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("응답 결과 (ERROR)"),
                                fieldWithPath("data").type(JsonFieldType.NULL).optional()
                                        .description("응답 데이터 (에러 시 null)"),
                                fieldWithPath("error").type(JsonFieldType.OBJECT).description("에러 정보"),
                                fieldWithPath("error.code").type(JsonFieldType.STRING).description("에러 코드"),
                                fieldWithPath("error.message").type(JsonFieldType.STRING).description("에러 메시지"),
                                fieldWithPath("error.data").type(JsonFieldType.OBJECT).description("에러 상세 정보").optional()
                        )
                ));
    }
}

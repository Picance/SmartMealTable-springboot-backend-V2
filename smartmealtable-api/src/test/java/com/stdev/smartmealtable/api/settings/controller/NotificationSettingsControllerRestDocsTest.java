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
import com.stdev.smartmealtable.domain.settings.entity.NotificationSettings;
import com.stdev.smartmealtable.domain.settings.repository.NotificationSettingsRepository;
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
 * 알림 설정 API REST Docs 테스트
 */
@Transactional
@DisplayName("NotificationSettingsController REST Docs 테스트")
class NotificationSettingsControllerRestDocsTest extends AbstractRestDocsTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberAuthenticationRepository memberAuthenticationRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private NotificationSettingsRepository notificationSettingsRepository;

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

        // 테스트용 알림 설정 생성
        NotificationSettings notificationSettings = NotificationSettings.create(testMemberId);
        notificationSettingsRepository.save(notificationSettings);
    }

    @Test
    @DisplayName("[Docs] 알림 설정 조회 성공")
    void getNotificationSettings_Success_Docs() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/members/me/notification-settings")
                        .header("Authorization", createAccessToken(testMemberId)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.pushEnabled").value(true))
                .andExpect(jsonPath("$.data.storeNoticeEnabled").value(true))
                .andDo(document("notification-settings/get",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("응답 결과 (SUCCESS/ERROR)"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.pushEnabled").type(JsonFieldType.BOOLEAN).description("전체 푸시 알림 활성화 여부"),
                                fieldWithPath("data.storeNoticeEnabled").type(JsonFieldType.BOOLEAN).description("가게 공지 알림 활성화 여부"),
                                fieldWithPath("data.recommendationEnabled").type(JsonFieldType.BOOLEAN).description("음식점 추천 알림 활성화 여부"),
                                fieldWithPath("data.budgetAlertEnabled").type(JsonFieldType.BOOLEAN).description("예산 알림 활성화 여부"),
                                fieldWithPath("data.passwordExpiryAlertEnabled").type(JsonFieldType.BOOLEAN).description("비밀번호 만료 알림 활성화 여부"),
                                fieldWithPath("error").type(JsonFieldType.NULL).optional()
                                        .description("에러 정보 (성공 시 null)")
                        )
                ));
    }

    @Test
    @DisplayName("[Docs] 알림 설정 변경 성공")
    void updateNotificationSettings_Success_Docs() throws Exception {
        // given
        String requestBody = """
                {
                    "pushEnabled": false,
                    "storeNoticeEnabled": false,
                    "recommendationEnabled": false,
                    "budgetAlertEnabled": true,
                    "passwordExpiryAlertEnabled": true
                }
                """;

        // when & then
        mockMvc.perform(put("/api/v1/members/me/notification-settings")
                        .header("Authorization", createAccessToken(testMemberId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.pushEnabled").value(false))
                .andExpect(jsonPath("$.data.storeNoticeEnabled").value(false))
                .andDo(document("notification-settings/update",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        authorizationHeader(),
                        requestFields(
                                fieldWithPath("pushEnabled").type(JsonFieldType.BOOLEAN).description("전체 푸시 알림 활성화 여부"),
                                fieldWithPath("storeNoticeEnabled").type(JsonFieldType.BOOLEAN).description("가게 공지 알림 활성화 여부"),
                                fieldWithPath("recommendationEnabled").type(JsonFieldType.BOOLEAN).description("음식점 추천 알림 활성화 여부"),
                                fieldWithPath("budgetAlertEnabled").type(JsonFieldType.BOOLEAN).description("예산 알림 활성화 여부"),
                                fieldWithPath("passwordExpiryAlertEnabled").type(JsonFieldType.BOOLEAN).description("비밀번호 만료 알림 활성화 여부")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("응답 결과 (SUCCESS/ERROR)"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.pushEnabled").type(JsonFieldType.BOOLEAN).description("전체 푸시 알림 활성화 여부"),
                                fieldWithPath("data.storeNoticeEnabled").type(JsonFieldType.BOOLEAN).description("가게 공지 알림 활성화 여부"),
                                fieldWithPath("data.recommendationEnabled").type(JsonFieldType.BOOLEAN).description("음식점 추천 알림 활성화 여부"),
                                fieldWithPath("data.budgetAlertEnabled").type(JsonFieldType.BOOLEAN).description("예산 알림 활성화 여부"),
                                fieldWithPath("data.passwordExpiryAlertEnabled").type(JsonFieldType.BOOLEAN).description("비밀번호 만료 알림 활성화 여부"),
                                fieldWithPath("error").type(JsonFieldType.NULL).optional()
                                        .description("에러 정보 (성공 시 null)")
                        )
                ));
    }

    @Test
    @DisplayName("[Docs] 알림 설정 변경 실패 - 필수 필드 누락")
    void updateNotificationSettings_Fail_MissingField_Docs() throws Exception {
        // given
        String invalidRequest = """
                {
                    "pushEnabled": true,
                    "storeNoticeEnabled": true
                }
                """;

        // when & then
        mockMvc.perform(put("/api/v1/members/me/notification-settings")
                        .header("Authorization", createAccessToken(testMemberId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andDo(document("notification-settings/update-fail-missing-field",
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
    @DisplayName("[Docs] 알림 설정 조회 실패 - 인증 토큰 없음")
    void getNotificationSettings_Fail_NoAuth_Docs() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/members/me/notification-settings"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andDo(document("notification-settings/get-fail-no-auth",
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

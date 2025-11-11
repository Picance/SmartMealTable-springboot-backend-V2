package com.stdev.smartmealtable.api.onboarding.controller;

import com.stdev.smartmealtable.domain.common.vo.Address;
import com.stdev.smartmealtable.domain.common.vo.AddressType;
import com.stdev.smartmealtable.api.common.AbstractRestDocsTest;
import com.stdev.smartmealtable.api.onboarding.dto.request.OnboardingProfileRequest;
import com.stdev.smartmealtable.domain.member.entity.Group;
import com.stdev.smartmealtable.domain.member.entity.GroupType;
import com.stdev.smartmealtable.domain.member.entity.Member;
import com.stdev.smartmealtable.domain.member.entity.MemberAuthentication;
import com.stdev.smartmealtable.domain.member.entity.RecommendationType;
import com.stdev.smartmealtable.domain.member.repository.GroupRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberAuthenticationRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberRepository;
import com.stdev.smartmealtable.support.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 온보딩 - 프로필 설정 API Rest Docs 문서화 테스트
 */
@Transactional
class OnboardingProfileControllerRestDocsTest extends AbstractRestDocsTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberAuthenticationRepository memberAuthenticationRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private Long testMemberId;
    private Long testGroupId;

    @BeforeEach
    void setUp() {
        // 테스트용 그룹 생성
        Group testGroup = Group.create("서울대학교", GroupType.UNIVERSITY, Address.of("서울대학교", null, "서울특별시 관악구 관악로 1", null, null, null, null));
        Group savedGroup = groupRepository.save(testGroup);
        testGroupId = savedGroup.getGroupId();

        // 테스트용 회원 생성
        Member testMember = Member.create(null, "임시닉네임_" + System.currentTimeMillis(), null, RecommendationType.BALANCED);
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
    }

    /**
     * Authorization 헤더 생성 헬퍼 메서드
     */
    private String createAuthorizationHeader(Long memberId) {
        String token = jwtTokenProvider.createToken(memberId);
        return "Bearer " + token;
    }

    @Test
    @DisplayName("[Docs] 온보딩 - 프로필 설정 성공")
    void onboardingProfile_Success_Docs() throws Exception {
        // given
        OnboardingProfileRequest request = new OnboardingProfileRequest("길동이", testGroupId);

        // when & then
        mockMvc.perform(post("/api/v1/onboarding/profile")
                        .header("Authorization", createAuthorizationHeader(testMemberId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("onboarding/profile/success",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("JWT 액세스 토큰 (Bearer 스킴). 인증된 사용자의 memberId를 포함")
                        ),
                        requestFields(
                                fieldWithPath("nickname").type(JsonFieldType.STRING)
                                        .description("설정할 닉네임 (2-50자)"),
                                fieldWithPath("groupId").type(JsonFieldType.NUMBER)
                                        .description("소속 그룹 ID (대학교, 회사 등)")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING)
                                        .description("응답 결과 (SUCCESS/ERROR)"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT)
                                        .description("응답 데이터"),
                                fieldWithPath("data.memberId").type(JsonFieldType.NUMBER)
                                        .description("회원 ID"),
                                fieldWithPath("data.nickname").type(JsonFieldType.STRING)
                                        .description("설정된 닉네임"),
                                fieldWithPath("data.group").type(JsonFieldType.OBJECT)
                                        .description("소속 그룹 정보"),
                                fieldWithPath("data.group.groupId").type(JsonFieldType.NUMBER)
                                        .description("그룹 ID"),
                                fieldWithPath("data.group.name").type(JsonFieldType.STRING)
                                        .description("그룹명"),
                                fieldWithPath("data.group.type").type(JsonFieldType.STRING)
                                        .description("그룹 유형 (UNIVERSITY, COMPANY, OTHER)"),
                                fieldWithPath("data.group.address").type(JsonFieldType.STRING)
                                        .description("그룹 주소"),
                                fieldWithPath("error").type(JsonFieldType.NULL).optional()
                                        .description("에러 정보 (성공 시 null)")
                        )
                ));
    }

    @Test
    @DisplayName("[Docs] 온보딩 - 프로필 설정 실패 (닉네임 중복)")
    void onboardingProfile_Fail_DuplicateNickname_Docs() throws Exception {
        // given - 이미 사용 중인 닉네임으로 회원 생성
        Member existingMember = Member.create(testGroupId, "중복닉네임", null, RecommendationType.BALANCED);
        memberRepository.save(existingMember);

        OnboardingProfileRequest request = new OnboardingProfileRequest("중복닉네임", testGroupId);

        // when & then
        mockMvc.perform(post("/api/v1/onboarding/profile")
                        .header("Authorization", createAuthorizationHeader(testMemberId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andDo(document("onboarding/profile/fail-duplicate-nickname",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING)
                                        .description("응답 결과 (ERROR)"),
                                fieldWithPath("data").type(JsonFieldType.NULL).optional()
                                        .description("응답 데이터 (에러 시 null)"),
                                fieldWithPath("error").type(JsonFieldType.OBJECT)
                                        .description("에러 정보"),
                                fieldWithPath("error.code").type(JsonFieldType.STRING)
                                        .description("에러 코드 (E409)"),
                                fieldWithPath("error.message").type(JsonFieldType.STRING)
                                        .description("에러 메시지")
                        )
                ));
    }

    @Test
    @DisplayName("[Docs] 온보딩 - 프로필 설정 실패 (그룹 미존재)")
    void onboardingProfile_Fail_GroupNotFound_Docs() throws Exception {
        // given
        OnboardingProfileRequest request = new OnboardingProfileRequest("길동이", 99999L);

        // when & then
        mockMvc.perform(post("/api/v1/onboarding/profile")
                        .header("Authorization", createAuthorizationHeader(testMemberId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andDo(document("onboarding/profile/fail-group-not-found",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING)
                                        .description("응답 결과 (ERROR)"),
                                fieldWithPath("data").type(JsonFieldType.NULL).optional()
                                        .description("응답 데이터 (에러 시 null)"),
                                fieldWithPath("error").type(JsonFieldType.OBJECT)
                                        .description("에러 정보"),
                                fieldWithPath("error.code").type(JsonFieldType.STRING)
                                        .description("에러 코드 (E404)"),
                                fieldWithPath("error.message").type(JsonFieldType.STRING)
                                        .description("에러 메시지")
                        )
                ));
    }
}

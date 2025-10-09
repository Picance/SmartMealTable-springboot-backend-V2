package com.stdev.smartmealtable.api.onboarding.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stdev.smartmealtable.api.common.AbstractContainerTest;
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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 온보딩 - 프로필 설정 API 통합 테스트
 * TDD: RED-GREEN-REFACTOR
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OnboardingProfileControllerTest extends AbstractContainerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberAuthenticationRepository memberAuthenticationRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private Long authenticatedMemberId;
    private Long testGroupId;

    @BeforeEach
    void setUp() {
        // 테스트용 그룹 생성
        Group testGroup = Group.create("서울대학교", GroupType.UNIVERSITY, "서울특별시 관악구 관악로 1");
        Group savedGroup = groupRepository.save(testGroup);
        testGroupId = savedGroup.getGroupId();

        // 테스트용 회원 생성 (온보딩 미완료 상태: 그룹 미설정, 닉네임은 임시값)
        Member testMember = Member.create(null, "임시닉네임_" + System.currentTimeMillis(), RecommendationType.BALANCED);
        Member savedMember = memberRepository.save(testMember);
        authenticatedMemberId = savedMember.getMemberId();

        // 테스트용 인증 정보 생성
        MemberAuthentication auth = MemberAuthentication.createEmailAuth(
                savedMember.getMemberId(),
                "test@example.com",
                "hashedPasswordForTest",
                "테스트유저"
        );
        memberAuthenticationRepository.save(auth);
    }

    @Test
    @DisplayName("온보딩 프로필 설정 성공 - 닉네임과 소속 그룹 설정")
    void onboardingProfile_Success() throws Exception {
        // given
        OnboardingProfileRequest request = new OnboardingProfileRequest("길동이", testGroupId);

        // when & then
        mockMvc.perform(post("/api/v1/onboarding/profile")
                        .header("Authorization", createAuthorizationHeader(authenticatedMemberId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.memberId").value(authenticatedMemberId))
                .andExpect(jsonPath("$.data.nickname").value("길동이"))
                .andExpect(jsonPath("$.data.group.groupId").value(testGroupId))
                .andExpect(jsonPath("$.data.group.name").value("서울대학교"))
                .andExpect(jsonPath("$.data.group.type").value("UNIVERSITY"));
                // error 필드는 @JsonInclude(NON_NULL)에 의해 JSON에서 제외됨
    }

        @Test
    @DisplayName("온보딩 프로필 설정 실패 - 닉네임 중복")
    void onboardingProfile_Fail_DuplicateNickname() throws Exception {
        // given - 이미 사용 중인 닉네임으로 회원 생성
        Member existingMember = Member.create(testGroupId, "중복닉네임", RecommendationType.BALANCED);
        memberRepository.save(existingMember);

        OnboardingProfileRequest request = new OnboardingProfileRequest("중복닉네임", testGroupId);

        // when & then
        mockMvc.perform(post("/api/v1/onboarding/profile")
                        .header("Authorization", createAuthorizationHeader(authenticatedMemberId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E409"))
                .andExpect(jsonPath("$.error.message").value("이미 사용 중인 닉네임입니다."));
    }

    @Test
    @DisplayName("온보딩 프로필 설정 실패 - 존재하지 않는 그룹")
    void onboardingProfile_Fail_GroupNotFound() throws Exception {
        // given
        Long nonExistentGroupId = 99999L;
        OnboardingProfileRequest request = new OnboardingProfileRequest("길동이", nonExistentGroupId);

        // when & then
        mockMvc.perform(post("/api/v1/onboarding/profile")
                        .header("Authorization", createAuthorizationHeader(authenticatedMemberId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E404"))
                .andExpect(jsonPath("$.error.message").value("존재하지 않는 그룹입니다."));
    }

    @Test
    @DisplayName("온보딩 프로필 설정 실패 - 닉네임 길이 제한 (1자)")
    void onboardingProfile_Fail_NicknameTooShort() throws Exception {
        // given
        OnboardingProfileRequest request = new OnboardingProfileRequest("김", testGroupId);

        // when & then
        mockMvc.perform(post("/api/v1/onboarding/profile")
                        .header("Authorization", createAuthorizationHeader(authenticatedMemberId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E422"));
    }

    @Test
    @DisplayName("온보딩 프로필 설정 실패 - 닉네임 null")
    void onboardingProfile_Fail_NicknameNull() throws Exception {
        // given
        OnboardingProfileRequest request = new OnboardingProfileRequest(null, testGroupId);

        // when & then
        mockMvc.perform(post("/api/v1/onboarding/profile")
                        .header("Authorization", createAuthorizationHeader(authenticatedMemberId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E422"));
    }

    @Test
    @DisplayName("온보딩 프로필 설정 실패 - groupId null")
    void onboardingProfile_Fail_GroupIdNull() throws Exception {
        // given
        OnboardingProfileRequest request = new OnboardingProfileRequest("길동이", null);

        // when & then
        mockMvc.perform(post("/api/v1/onboarding/profile")
                        .header("Authorization", createAuthorizationHeader(authenticatedMemberId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E422"));
    }

    // === Helper Methods ===

    /**
     * JWT 토큰을 생성하여 "Bearer {token}" 형식으로 반환
     */
    private String createAuthorizationHeader(Long memberId) {
        String token = jwtTokenProvider.createToken(memberId);
        return "Bearer " + token;
    }
}

package com.stdev.smartmealtable.api.member.controller;

import com.stdev.smartmealtable.api.common.AbstractContainerTest;
import com.stdev.smartmealtable.domain.member.entity.*;
import com.stdev.smartmealtable.domain.member.repository.MemberAuthenticationRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberRepository;
import com.stdev.smartmealtable.domain.member.repository.SocialAccountRepository;
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

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 소셜 계정 목록 조회 API 통합 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GetSocialAccountListControllerTest extends AbstractContainerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberAuthenticationRepository memberAuthenticationRepository;

    @Autowired
    private SocialAccountRepository socialAccountRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private Member testMember;
    private MemberAuthentication testAuth;
    private String accessToken;

    @BeforeEach
    void setUp() {
        // 1. 회원 생성
        testMember = Member.create(null, "테스트닉네임", null, RecommendationType.BALANCED);
        testMember = memberRepository.save(testMember);

        // 2. 회원 인증 정보 생성 (소셜 로그인)
        testAuth = MemberAuthentication.createSocialAuth(
                testMember.getMemberId(),
                "[email protected]",
                "소셜유저"
        );
        testAuth = memberAuthenticationRepository.save(testAuth);

        // 3. 소셜 계정 2개 생성
        SocialAccount kakaoAccount = SocialAccount.create(
                testAuth.getMemberAuthenticationId(),
                SocialProvider.KAKAO,
                "kakao123",
                "kakao_access_token",
                "kakao_refresh_token",
                "Bearer",
                LocalDateTime.now().plusHours(1)
        );
        socialAccountRepository.save(kakaoAccount);

        SocialAccount googleAccount = SocialAccount.create(
                testAuth.getMemberAuthenticationId(),
                SocialProvider.GOOGLE,
                "google456",
                "google_access_token",
                "google_refresh_token",
                "Bearer",
                LocalDateTime.now().plusHours(1)
        );
        socialAccountRepository.save(googleAccount);

        // 4. JWT Access Token 생성
        accessToken = jwtTokenProvider.createToken(testMember.getMemberId());
    }

    @Test
    @DisplayName("소셜 계정 목록 조회 성공 - 비밀번호 없음")
    void getSocialAccountList_Success_NoPassword() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/members/me/social-accounts")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.connectedAccounts", hasSize(2)))
                .andExpect(jsonPath("$.data.connectedAccounts[0].provider", anyOf(is("KAKAO"), is("GOOGLE"))))
                .andExpect(jsonPath("$.data.connectedAccounts[0].providerEmail").value("[email protected]"))
                .andExpect(jsonPath("$.data.connectedAccounts[0].connectedAt").isNotEmpty())
                .andExpect(jsonPath("$.data.hasPassword").value(false));
    }

    @Test
    @DisplayName("소셜 계정 목록 조회 성공 - 비밀번호 있음")
    void getSocialAccountList_Success_WithPassword() throws Exception {
        // given: 비밀번호 설정
        testAuth.changePassword("hashed_new_password");
        memberAuthenticationRepository.save(testAuth);

        // when & then
        mockMvc.perform(get("/api/v1/members/me/social-accounts")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.connectedAccounts", hasSize(2)))
                .andExpect(jsonPath("$.data.hasPassword").value(true));
    }

    @Test
    @DisplayName("소셜 계정 목록 조회 성공 - 소셜 계정 없음")
    void getSocialAccountList_Success_NoSocialAccounts() throws Exception {
        // given: 소셜 계정 삭제
        socialAccountRepository.findByMemberAuthenticationId(testAuth.getMemberAuthenticationId())
                .forEach(socialAccountRepository::delete);

        // when & then
        mockMvc.perform(get("/api/v1/members/me/social-accounts")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.connectedAccounts", hasSize(0)))
                .andExpect(jsonPath("$.data.hasPassword").value(false));
    }
}

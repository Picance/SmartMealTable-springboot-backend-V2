package com.stdev.smartmealtable.api.auth.service;

import com.stdev.smartmealtable.api.auth.dto.request.KakaoLoginServiceRequest;
import com.stdev.smartmealtable.api.auth.dto.response.KakaoLoginServiceResponse;
import com.stdev.smartmealtable.client.auth.oauth.dto.OAuthTokenResponse;
import com.stdev.smartmealtable.client.auth.oauth.dto.OAuthUserInfo;
import com.stdev.smartmealtable.client.auth.oauth.kakao.KakaoAuthClient;
import com.stdev.smartmealtable.domain.member.entity.Member;
import com.stdev.smartmealtable.domain.member.entity.MemberAuthentication;
import com.stdev.smartmealtable.domain.member.entity.RecommendationType;
import com.stdev.smartmealtable.domain.member.entity.SocialAccount;
import com.stdev.smartmealtable.domain.member.entity.SocialProvider;
import com.stdev.smartmealtable.domain.member.repository.MemberAuthenticationRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberRepository;
import com.stdev.smartmealtable.domain.member.repository.SocialAccountRepository;
import com.stdev.smartmealtable.domain.member.service.SocialAuthDomainService;
import com.stdev.smartmealtable.support.jwt.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * KakaoLoginService 테스트
 */
@ExtendWith(MockitoExtension.class)
class KakaoLoginServiceTest {

    @Mock
    private KakaoAuthClient kakaoAuthClient;

    @Mock
    private SocialAuthDomainService socialAuthDomainService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberAuthenticationRepository memberAuthenticationRepository;

    @Mock
    private SocialAccountRepository socialAccountRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private KakaoLoginService kakaoLoginService;

    @Test
    @DisplayName("신규 회원 카카오 로그인 성공")
    void loginWithKakao_NewMember_Success() {
        // given
        var request = new KakaoLoginServiceRequest(
                "test-authorization-code",
                "http://localhost:3000/auth/callback"
        );

        var tokenResponse = new OAuthTokenResponse(
                "kakao-access-token",
                "kakao-refresh-token",
                3600,
                "Bearer",
                "kakao-id-token"
        );

        var userInfo = new OAuthUserInfo(
                "kakao-user-id-12345",
                "[email protected]",
                "카카오유저",
                null
        );

        var member = Member.reconstitute(1L, null, "카카오유저", null, RecommendationType.BALANCED);

        given(kakaoAuthClient.getAccessToken(anyString()))
                .willReturn(tokenResponse);
        given(kakaoAuthClient.extractUserInfo(anyString()))
                .willReturn(userInfo);
        given(socialAccountRepository.findByProviderAndProviderId(any(), anyString()))
                .willReturn(Optional.empty());
        given(socialAuthDomainService.createMemberWithSocialAccount(
                anyString(),
                anyString(),
                any(),
                any(SocialProvider.class),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                any(LocalDateTime.class)
        )).willReturn(member);
        given(jwtTokenProvider.createToken(1L))
                .willReturn("mock-jwt-access-token")
                .willReturn("mock-jwt-refresh-token");

        // when
        var response = kakaoLoginService.login(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("mock-jwt-access-token");
        assertThat(response.refreshToken()).isEqualTo("mock-jwt-refresh-token");
        assertThat(response.email()).isEqualTo("[email protected]");
        assertThat(response.name()).isEqualTo("카카오유저");
        assertThat(response.isNewMember()).isTrue();

        verify(kakaoAuthClient, times(1)).getAccessToken(anyString());
        verify(kakaoAuthClient, times(1)).extractUserInfo(anyString());
        verify(socialAccountRepository, times(1)).findByProviderAndProviderId(any(), anyString());
        verify(socialAuthDomainService, times(1)).createMemberWithSocialAccount(
                anyString(),
                anyString(),
                any(),
                any(SocialProvider.class),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                any(LocalDateTime.class)
        );
        verify(jwtTokenProvider, times(2)).createToken(1L);
    }

    @Test
    @DisplayName("기존 회원 카카오 로그인 성공")
    void loginWithKakao_ExistingMember_Success() {
        // given
        var request = new KakaoLoginServiceRequest(
                "test-authorization-code",
                "http://localhost:3000/auth/callback"
        );

        var tokenResponse = new OAuthTokenResponse(
                "kakao-access-token",
                "kakao-refresh-token",
                3600,
                "Bearer",
                "kakao-id-token"
        );

        var userInfo = new OAuthUserInfo(
                "kakao-user-id-12345",
                "[email protected]",
                "카카오유저",
                null
        );

        var existingSocialAccount = SocialAccount.create(
                1L,
                SocialProvider.KAKAO,
                "kakao-user-id-12345",
                "old-access-token",
                "old-refresh-token",
                "Bearer",
                LocalDateTime.now().plusSeconds(3600)
        );

        var memberAuth = MemberAuthentication.reconstitute(
                1L, // memberAuthenticationId
                1L, // memberId
                "[email protected]",
                null, // hashedPassword (소셜 로그인이므로 null)
                0, // failureCount
                null, // passwordChangedAt
                null, // passwordExpiresAt
                "카카오유저",
                null, // deletedAt
                LocalDateTime.now()
        );
        var member = Member.reconstitute(1L, null, "카카오유저", null, RecommendationType.BALANCED);

        given(kakaoAuthClient.getAccessToken(anyString()))
                .willReturn(tokenResponse);
        given(kakaoAuthClient.extractUserInfo(anyString()))
                .willReturn(userInfo);
        given(socialAccountRepository.findByProviderAndProviderId(any(), anyString()))
                .willReturn(Optional.of(existingSocialAccount));
        given(memberAuthenticationRepository.findById(any()))
                .willReturn(Optional.of(memberAuth));
        given(memberRepository.findById(any()))
                .willReturn(Optional.of(member));
        given(jwtTokenProvider.createToken(1L))
                .willReturn("mock-jwt-access-token")
                .willReturn("mock-jwt-refresh-token");

        // when
        var response = kakaoLoginService.login(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("mock-jwt-access-token");
        assertThat(response.refreshToken()).isEqualTo("mock-jwt-refresh-token");
        assertThat(response.email()).isEqualTo("[email protected]");
        assertThat(response.name()).isEqualTo("카카오유저");
        assertThat(response.isNewMember()).isFalse();

        verify(kakaoAuthClient, times(1)).getAccessToken(anyString());
        verify(kakaoAuthClient, times(1)).extractUserInfo(anyString());
        verify(socialAccountRepository, times(1)).findByProviderAndProviderId(any(), anyString());
        verify(socialAuthDomainService, times(1)).updateSocialAccountToken(
                any(SocialProvider.class),
                anyString(),
                anyString(),
                anyString(),
                any(LocalDateTime.class)
        );
        verify(jwtTokenProvider, times(2)).createToken(1L);
    }

    @Test
    @DisplayName("이메일 회원가입 후 동일 이메일로 카카오 로그인 시 계정 연동")
    void loginWithKakao_LinkToEmailAccount_Success() {
        // given
        var request = new KakaoLoginServiceRequest(
                "test-authorization-code",
                "http://localhost:3000/auth/callback"
        );

        var tokenResponse = new OAuthTokenResponse(
                "kakao-access-token",
                "kakao-refresh-token",
                3600,
                "Bearer",
                "kakao-id-token"
        );

        var userInfo = new OAuthUserInfo(
                "kakao-user-id-12345",
                "qwe123@example.com",  // 기존 이메일 회원과 동일
                "카카오유저",
                "https://kakao.com/profile.jpg"
        );

        // 기존 이메일 회원
        var existingMember = Member.reconstitute(
                100L,
                null,
                "qwe123",
                null,
                RecommendationType.BALANCED
        );

        given(kakaoAuthClient.getAccessToken(anyString()))
                .willReturn(tokenResponse);
        given(kakaoAuthClient.extractUserInfo(anyString()))
                .willReturn(userInfo);
        // 소셜 계정은 존재하지 않음
        given(socialAccountRepository.findByProviderAndProviderId(any(), anyString()))
                .willReturn(Optional.empty());
        // createMemberWithSocialAccount가 기존 회원과 연동
        given(socialAuthDomainService.createMemberWithSocialAccount(
                anyString(),
                anyString(),
                any(),
                any(SocialProvider.class),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                any(LocalDateTime.class)
        )).willReturn(existingMember);
        given(jwtTokenProvider.createToken(100L))
                .willReturn("mock-jwt-access-token")
                .willReturn("mock-jwt-refresh-token");

        // when
        var response = kakaoLoginService.login(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("mock-jwt-access-token");
        assertThat(response.refreshToken()).isEqualTo("mock-jwt-refresh-token");
        assertThat(response.memberId()).isEqualTo(100L);  // 기존 회원 ID
        assertThat(response.email()).isEqualTo("qwe123@example.com");
        assertThat(response.isNewMember()).isTrue();  // 소셜 계정은 신규

        verify(kakaoAuthClient, times(1)).getAccessToken(anyString());
        verify(kakaoAuthClient, times(1)).extractUserInfo(anyString());
        verify(socialAccountRepository, times(1)).findByProviderAndProviderId(any(), anyString());
        verify(socialAuthDomainService, times(1)).createMemberWithSocialAccount(
                anyString(),
                anyString(),
                any(),
                any(SocialProvider.class),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                any(LocalDateTime.class)
        );
        verify(jwtTokenProvider, times(2)).createToken(100L);
    }
}

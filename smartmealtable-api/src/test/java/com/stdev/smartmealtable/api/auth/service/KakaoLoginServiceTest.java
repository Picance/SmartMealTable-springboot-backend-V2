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

        var member = Member.create(null, "카카오유저", RecommendationType.BALANCED);

        given(kakaoAuthClient.getAccessToken(anyString()))
                .willReturn(tokenResponse);
        given(kakaoAuthClient.extractUserInfo(anyString()))
                .willReturn(userInfo);
        given(socialAccountRepository.findByProviderAndProviderId(any(), anyString()))
                .willReturn(Optional.empty());
        given(socialAuthDomainService.createMemberWithSocialAccount(
                anyString(),
                anyString(),
                any(SocialProvider.class),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                any(LocalDateTime.class)
        )).willReturn(member);

        // when
        var response = kakaoLoginService.login(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.email()).isEqualTo("[email protected]");
        assertThat(response.name()).isEqualTo("카카오유저");
        assertThat(response.isNewMember()).isTrue();

        verify(kakaoAuthClient, times(1)).getAccessToken(anyString());
        verify(kakaoAuthClient, times(1)).extractUserInfo(anyString());
        verify(socialAccountRepository, times(1)).findByProviderAndProviderId(any(), anyString());
        verify(socialAuthDomainService, times(1)).createMemberWithSocialAccount(
                anyString(),
                anyString(),
                any(SocialProvider.class),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                any(LocalDateTime.class)
        );
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

        var memberAuth = MemberAuthentication.createSocialAuth(1L, "[email protected]", "카카오유저");
        var member = Member.create(null, "카카오유저", RecommendationType.BALANCED);

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

        // when
        var response = kakaoLoginService.login(request);

        // then
        assertThat(response).isNotNull();
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
    }
}

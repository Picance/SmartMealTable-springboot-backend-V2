package com.stdev.smartmealtable.api.auth.service;

import com.stdev.smartmealtable.api.auth.dto.request.GoogleLoginServiceRequest;
import com.stdev.smartmealtable.api.auth.dto.response.GoogleLoginServiceResponse;
import com.stdev.smartmealtable.client.auth.oauth.dto.OAuthTokenResponse;
import com.stdev.smartmealtable.client.auth.oauth.dto.OAuthUserInfo;
import com.stdev.smartmealtable.client.auth.oauth.google.GoogleAuthClient;
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
 * GoogleLoginService 테스트
 */
@ExtendWith(MockitoExtension.class)
class GoogleLoginServiceTest {

    @Mock
    private GoogleAuthClient googleAuthClient;

    @Mock
    private SocialAuthDomainService socialAuthDomainService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberAuthenticationRepository memberAuthenticationRepository;

    @Mock
    private SocialAccountRepository socialAccountRepository;

    @InjectMocks
    private GoogleLoginService googleLoginService;

    @Test
    @DisplayName("신규 회원 구글 로그인 성공")
    void loginWithGoogle_NewMember_Success() {
        // given
        var request = new GoogleLoginServiceRequest(
                "test-authorization-code",
                "http://localhost:5173/oauth/google/callback"
        );

        var tokenResponse = new OAuthTokenResponse(
                "google-access-token",
                "google-refresh-token",
                3600,
                "Bearer",
                "google-id-token"
        );

        var userInfo = new OAuthUserInfo(
                "google-user-id-12345",
                "[email protected]",
                "구글유저",
                "https://example.com/profile.jpg"
        );

        var member = Member.create(null, "구글유저", null, RecommendationType.BALANCED);

        given(googleAuthClient.getAccessToken(anyString()))
                .willReturn(tokenResponse);
        given(googleAuthClient.extractUserInfo(anyString()))
                .willReturn(userInfo);
        given(socialAccountRepository.findByProviderAndProviderId(any(), anyString()))
                .willReturn(Optional.empty());
        given(socialAuthDomainService.createMemberWithSocialAccount(
                anyString(),
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
        var response = googleLoginService.login(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.email()).isEqualTo("[email protected]");
        assertThat(response.name()).isEqualTo("구글유저");
        assertThat(response.isNewMember()).isTrue();

        verify(googleAuthClient, times(1)).getAccessToken(anyString());
        verify(googleAuthClient, times(1)).extractUserInfo(anyString());
        verify(socialAccountRepository, times(1)).findByProviderAndProviderId(any(), anyString());
        verify(socialAuthDomainService, times(1)).createMemberWithSocialAccount(
                anyString(),
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
    @DisplayName("기존 회원 구글 로그인 성공")
    void loginWithGoogle_ExistingMember_Success() {
        // given
        var request = new GoogleLoginServiceRequest(
                "test-authorization-code",
                "http://localhost:5173/oauth/google/callback"
        );

        var tokenResponse = new OAuthTokenResponse(
                "google-access-token",
                "google-refresh-token",
                3600,
                "Bearer",
                "google-id-token"
        );

        var userInfo = new OAuthUserInfo(
                "google-user-id-12345",
                "[email protected]",
                "구글유저",
                "https://example.com/profile.jpg"
        );

        var existingSocialAccount = SocialAccount.create(
                1L,
                SocialProvider.GOOGLE,
                "google-user-id-12345",
                "old-access-token",
                "old-refresh-token",
                "Bearer",
                LocalDateTime.now().plusSeconds(3600)
        );

        var memberAuth = MemberAuthentication.createSocialAuth(1L, "[email protected]", "구글유저");
        var member = Member.create(null, "구글유저", null, RecommendationType.BALANCED);

        given(googleAuthClient.getAccessToken(anyString()))
                .willReturn(tokenResponse);
        given(googleAuthClient.extractUserInfo(anyString()))
                .willReturn(userInfo);
        given(socialAccountRepository.findByProviderAndProviderId(any(), anyString()))
                .willReturn(Optional.of(existingSocialAccount));
        given(memberAuthenticationRepository.findById(any()))
                .willReturn(Optional.of(memberAuth));
        given(memberRepository.findById(any()))
                .willReturn(Optional.of(member));

        // when
        var response = googleLoginService.login(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.email()).isEqualTo("[email protected]");
        assertThat(response.name()).isEqualTo("구글유저");
        assertThat(response.isNewMember()).isFalse();

        verify(googleAuthClient, times(1)).getAccessToken(anyString());
        verify(googleAuthClient, times(1)).extractUserInfo(anyString());
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

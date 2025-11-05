package com.stdev.smartmealtable.domain.member.service;

import com.stdev.smartmealtable.domain.member.entity.*;
import com.stdev.smartmealtable.domain.member.repository.MemberAuthenticationRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberRepository;
import com.stdev.smartmealtable.domain.member.repository.SocialAccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

/**
 * SocialAuthDomainService 단위 테스트 (BDD Mockist 스타일)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SocialAuthDomainService 테스트")
class SocialAuthDomainServiceTest {

    @Mock
    private SocialAccountRepository socialAccountRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberAuthenticationRepository memberAuthenticationRepository;

    @InjectMocks
    private SocialAuthDomainService socialAuthDomainService;

    @Nested
    @DisplayName("findMemberBySocialAccount 메서드는")
    class Describe_findMemberBySocialAccount {

        @Nested
        @DisplayName("존재하는 소셜 계정이 주어지면")
        class Context_with_existing_social_account {

            @Test
            @DisplayName("연결된 회원 정보를 반환한다")
            void it_returns_member() {
                // Given
                SocialProvider provider = SocialProvider.KAKAO;
                String providerId = "kakao123456";

                SocialAccount socialAccount = SocialAccount.reconstitute(
                        1L,
                        1L,
                        provider,
                        providerId,
                        "access_token",
                        "refresh_token",
                        "Bearer",
                        LocalDateTime.now().plusHours(1),
                        LocalDateTime.now()
                );

                MemberAuthentication authentication = MemberAuthentication.reconstitute(
                        1L,
                        1L,
                        "test@example.com",
                        null,
                        0,
                        null,
                        null,
                        "홍길동",
                        null,
                        LocalDateTime.now()
                );

                Member member = Member.reconstitute(
                        1L,
                        null,
                        "홍길동",
                        null,
                        RecommendationType.BALANCED
                );

                given(socialAccountRepository.findByProviderAndProviderId(provider, providerId))
                        .willReturn(Optional.of(socialAccount));
                given(memberAuthenticationRepository.findById(1L))
                        .willReturn(Optional.of(authentication));
                given(memberRepository.findById(1L))
                        .willReturn(Optional.of(member));

                // When
                Optional<Member> result = socialAuthDomainService.findMemberBySocialAccount(provider, providerId);

                // Then
                assertThat(result).isPresent();
                assertThat(result.get().getMemberId()).isEqualTo(1L);
                then(socialAccountRepository).should(times(1)).findByProviderAndProviderId(provider, providerId);
            }
        }

        @Nested
        @DisplayName("존재하지 않는 소셜 계정이 주어지면")
        class Context_with_non_existing_social_account {

            @Test
            @DisplayName("Empty를 반환한다")
            void it_returns_empty() {
                // Given
                SocialProvider provider = SocialProvider.GOOGLE;
                String providerId = "google999999";

                given(socialAccountRepository.findByProviderAndProviderId(provider, providerId))
                        .willReturn(Optional.empty());

                // When
                Optional<Member> result = socialAuthDomainService.findMemberBySocialAccount(provider, providerId);

                // Then
                assertThat(result).isEmpty();
                then(socialAccountRepository).should(times(1)).findByProviderAndProviderId(provider, providerId);
            }
        }
    }

    @Nested
    @DisplayName("existsSocialAccount 메서드는")
    class Describe_existsSocialAccount {

        @Nested
        @DisplayName("존재하는 소셜 계정이 주어지면")
        class Context_with_existing_social_account {

            @Test
            @DisplayName("true를 반환한다")
            void it_returns_true() {
                // Given
                SocialProvider provider = SocialProvider.KAKAO;
                String providerId = "kakao123456";

                given(socialAccountRepository.existsByProviderAndProviderId(provider, providerId))
                        .willReturn(true);

                // When
                boolean result = socialAuthDomainService.existsSocialAccount(provider, providerId);

                // Then
                assertThat(result).isTrue();
                then(socialAccountRepository).should(times(1)).existsByProviderAndProviderId(provider, providerId);
            }
        }

        @Nested
        @DisplayName("존재하지 않는 소셜 계정이 주어지면")
        class Context_with_non_existing_social_account {

            @Test
            @DisplayName("false를 반환한다")
            void it_returns_false() {
                // Given
                SocialProvider provider = SocialProvider.GOOGLE;
                String providerId = "google999999";

                given(socialAccountRepository.existsByProviderAndProviderId(provider, providerId))
                        .willReturn(false);

                // When
                boolean result = socialAuthDomainService.existsSocialAccount(provider, providerId);

                // Then
                assertThat(result).isFalse();
                then(socialAccountRepository).should(times(1)).existsByProviderAndProviderId(provider, providerId);
            }
        }
    }

    @Nested
    @DisplayName("createMemberWithSocialAccount 메서드는")
    class Describe_createMemberWithSocialAccount {

        @Nested
        @DisplayName("유효한 소셜 계정 정보가 주어지면")
        class Context_with_valid_social_info {

            @Test
            @DisplayName("회원을 생성하고 소셜 계정과 연동한다")
            void it_creates_member_with_social_account() {
                // Given
                String email = "social@example.com";
                String name = "소셜유저";
                String profileImageUrl = "https://example.com/profile.jpg";
                SocialProvider provider = SocialProvider.KAKAO;
                String providerId = "kakao123456";
                String accessToken = "access_token_value";
                String refreshToken = "refresh_token_value";
                String tokenType = "Bearer";
                LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);

                Member savedMember = Member.reconstitute(
                        1L,
                        null,
                        name,
                        profileImageUrl,
                        RecommendationType.BALANCED
                );

                MemberAuthentication savedAuth = MemberAuthentication.reconstitute(
                        1L,
                        1L,
                        email,
                        null,
                        0,
                        null,
                        null,
                        name,
                        null,
                        LocalDateTime.now()
                );

                SocialAccount savedSocialAccount = SocialAccount.reconstitute(
                        1L,
                        1L,
                        provider,
                        providerId,
                        accessToken,
                        refreshToken,
                        tokenType,
                        expiresAt,
                        LocalDateTime.now()
                );

                given(memberRepository.save(any(Member.class))).willReturn(savedMember);
                given(memberAuthenticationRepository.save(any(MemberAuthentication.class))).willReturn(savedAuth);
                given(socialAccountRepository.save(any(SocialAccount.class))).willReturn(savedSocialAccount);

                // When
                Member result = socialAuthDomainService.createMemberWithSocialAccount(
                        email, name, profileImageUrl, provider, providerId, accessToken, refreshToken, tokenType, expiresAt
                );

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getMemberId()).isEqualTo(1L);
                assertThat(result.getNickname()).isEqualTo(name);
                assertThat(result.getProfileImageUrl()).isEqualTo(profileImageUrl);
                
                then(memberRepository).should(times(1)).save(any(Member.class));
                then(memberAuthenticationRepository).should(times(1)).save(any(MemberAuthentication.class));
                then(socialAccountRepository).should(times(1)).save(any(SocialAccount.class));
            }
        }

        @Nested
        @DisplayName("name이 null인 소셜 계정 정보가 주어지면")
        class Context_with_null_name {

            @Test
            @DisplayName("이메일 기반 닉네임으로 회원을 생성한다")
            void it_creates_member_with_email_based_nickname() {
                // Given
                String email = "testuser@example.com";
                String name = null;  // name이 null
                String profileImageUrl = null;
                SocialProvider provider = SocialProvider.KAKAO;
                String providerId = "kakao123456";
                String accessToken = "access_token_value";
                String refreshToken = "refresh_token_value";
                String tokenType = "Bearer";
                LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);

                String expectedNickname = "testuser";  // 이메일 앞부분

                Member savedMember = Member.reconstitute(
                        1L,
                        null,
                        expectedNickname,
                        profileImageUrl,
                        RecommendationType.BALANCED
                );

                MemberAuthentication savedAuth = MemberAuthentication.reconstitute(
                        1L,
                        1L,
                        email,
                        null,
                        0,
                        null,
                        null,
                        name,
                        null,
                        LocalDateTime.now()
                );

                SocialAccount savedSocialAccount = SocialAccount.reconstitute(
                        1L,
                        1L,
                        provider,
                        providerId,
                        accessToken,
                        refreshToken,
                        tokenType,
                        expiresAt,
                        LocalDateTime.now()
                );

                given(memberRepository.save(any(Member.class))).willReturn(savedMember);
                given(memberAuthenticationRepository.save(any(MemberAuthentication.class))).willReturn(savedAuth);
                given(socialAccountRepository.save(any(SocialAccount.class))).willReturn(savedSocialAccount);

                // When
                Member result = socialAuthDomainService.createMemberWithSocialAccount(
                        email, name, profileImageUrl, provider, providerId, accessToken, refreshToken, tokenType, expiresAt
                );

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getMemberId()).isEqualTo(1L);
                assertThat(result.getNickname()).isEqualTo(expectedNickname);
                
                then(memberRepository).should(times(1)).save(any(Member.class));
                then(memberAuthenticationRepository).should(times(1)).save(any(MemberAuthentication.class));
                then(socialAccountRepository).should(times(1)).save(any(SocialAccount.class));
            }
        }

        @Nested
        @DisplayName("특수문자가 포함된 이메일이 주어지면")
        class Context_with_special_characters_in_email {

            @Test
            @DisplayName("특수문자를 제거한 닉네임으로 회원을 생성한다")
            void it_creates_member_with_sanitized_nickname() {
                // Given
                String email = "test.user-123@example.com";
                String name = null;
                String profileImageUrl = null;
                SocialProvider provider = SocialProvider.KAKAO;
                String providerId = "kakao123456";
                String accessToken = "access_token_value";
                String refreshToken = "refresh_token_value";
                String tokenType = "Bearer";
                LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);

                String expectedNickname = "testuser123";  // 특수문자 제거됨

                Member savedMember = Member.reconstitute(
                        1L,
                        null,
                        expectedNickname,
                        profileImageUrl,
                        RecommendationType.BALANCED
                );

                MemberAuthentication savedAuth = MemberAuthentication.reconstitute(
                        1L,
                        1L,
                        email,
                        null,
                        0,
                        null,
                        null,
                        name,
                        null,
                        LocalDateTime.now()
                );

                SocialAccount savedSocialAccount = SocialAccount.reconstitute(
                        1L,
                        1L,
                        provider,
                        providerId,
                        accessToken,
                        refreshToken,
                        tokenType,
                        expiresAt,
                        LocalDateTime.now()
                );

                given(memberRepository.save(any(Member.class))).willReturn(savedMember);
                given(memberAuthenticationRepository.save(any(MemberAuthentication.class))).willReturn(savedAuth);
                given(socialAccountRepository.save(any(SocialAccount.class))).willReturn(savedSocialAccount);

                // When
                Member result = socialAuthDomainService.createMemberWithSocialAccount(
                        email, name, profileImageUrl, provider, providerId, accessToken, refreshToken, tokenType, expiresAt
                );

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getNickname()).isEqualTo(expectedNickname);
            }
        }
    }

    @Nested
    @DisplayName("updateSocialAccountToken 메서드는")
    class Describe_updateSocialAccountToken {

        @Nested
        @DisplayName("존재하는 소셜 계정에 새 토큰 정보가 주어지면")
        class Context_with_existing_account_and_new_token {

            @Test
            @DisplayName("토큰을 업데이트한다")
            void it_updates_token() {
                // Given
                SocialProvider provider = SocialProvider.KAKAO;
                String providerId = "kakao123456";
                String newAccessToken = "new_access_token";
                String newRefreshToken = "new_refresh_token";
                LocalDateTime newExpiresAt = LocalDateTime.now().plusHours(2);

                SocialAccount existingAccount = SocialAccount.reconstitute(
                        1L,
                        1L,
                        provider,
                        providerId,
                        "old_access_token",
                        "old_refresh_token",
                        "Bearer",
                        LocalDateTime.now().minusHours(1),
                        LocalDateTime.now()
                );

                given(socialAccountRepository.findByProviderAndProviderId(provider, providerId))
                        .willReturn(Optional.of(existingAccount));
                given(socialAccountRepository.save(any(SocialAccount.class)))
                        .willAnswer(invocation -> invocation.getArgument(0));

                // When
                socialAuthDomainService.updateSocialAccountToken(
                        provider, providerId, newAccessToken, newRefreshToken, newExpiresAt
                );

                // Then
                then(socialAccountRepository).should(times(1)).findByProviderAndProviderId(provider, providerId);
                then(socialAccountRepository).should(times(1)).save(any(SocialAccount.class));
            }
        }

        @Nested
        @DisplayName("존재하지 않는 소셜 계정에 토큰을 업데이트하려고 하면")
        class Context_with_non_existing_account {

            @Test
            @DisplayName("IllegalArgumentException을 발생시킨다")
            void it_throws_illegal_argument_exception() {
                // Given
                SocialProvider provider = SocialProvider.GOOGLE;
                String providerId = "google999999";
                String newAccessToken = "new_access_token";
                String newRefreshToken = "new_refresh_token";
                LocalDateTime newExpiresAt = LocalDateTime.now().plusHours(2);

                given(socialAccountRepository.findByProviderAndProviderId(provider, providerId))
                        .willReturn(Optional.empty());

                // When & Then
                assertThatThrownBy(() -> socialAuthDomainService.updateSocialAccountToken(
                        provider, providerId, newAccessToken, newRefreshToken, newExpiresAt
                ))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("소셜 계정을 찾을 수 없습니다.");

                then(socialAccountRepository).should(times(1)).findByProviderAndProviderId(provider, providerId);
                then(socialAccountRepository).should(times(0)).save(any(SocialAccount.class));
            }
        }
    }

    @Nested
    @DisplayName("findAuthenticationBySocialAccount 메서드는")
    class Describe_findAuthenticationBySocialAccount {

        @Nested
        @DisplayName("존재하는 소셜 계정이 주어지면")
        class Context_with_existing_social_account {

            @Test
            @DisplayName("연결된 회원 인증 정보를 반환한다")
            void it_returns_authentication() {
                // Given
                SocialProvider provider = SocialProvider.KAKAO;
                String providerId = "kakao123456";

                SocialAccount socialAccount = SocialAccount.reconstitute(
                        1L,
                        1L,
                        provider,
                        providerId,
                        "access_token",
                        "refresh_token",
                        "Bearer",
                        LocalDateTime.now().plusHours(1),
                        LocalDateTime.now()
                );

                MemberAuthentication authentication = MemberAuthentication.reconstitute(
                        1L,
                        1L,
                        "test@example.com",
                        null,
                        0,
                        null,
                        null,
                        "홍길동",
                        null,
                        LocalDateTime.now()
                );

                given(socialAccountRepository.findByProviderAndProviderId(provider, providerId))
                        .willReturn(Optional.of(socialAccount));
                given(memberAuthenticationRepository.findById(1L))
                        .willReturn(Optional.of(authentication));

                // When
                Optional<MemberAuthentication> result = socialAuthDomainService.findAuthenticationBySocialAccount(
                        provider, providerId
                );

                // Then
                assertThat(result).isPresent();
                assertThat(result.get().getMemberAuthenticationId()).isEqualTo(1L);
                then(socialAccountRepository).should(times(1)).findByProviderAndProviderId(provider, providerId);
                then(memberAuthenticationRepository).should(times(1)).findById(1L);
            }
        }

        @Nested
        @DisplayName("존재하지 않는 소셜 계정이 주어지면")
        class Context_with_non_existing_social_account {

            @Test
            @DisplayName("Empty를 반환한다")
            void it_returns_empty() {
                // Given
                SocialProvider provider = SocialProvider.GOOGLE;
                String providerId = "google999999";

                given(socialAccountRepository.findByProviderAndProviderId(provider, providerId))
                        .willReturn(Optional.empty());

                // When
                Optional<MemberAuthentication> result = socialAuthDomainService.findAuthenticationBySocialAccount(
                        provider, providerId
                );

                // Then
                assertThat(result).isEmpty();
                then(socialAccountRepository).should(times(1)).findByProviderAndProviderId(provider, providerId);
                then(memberAuthenticationRepository).should(times(0)).findById(any());
            }
        }
    }
}

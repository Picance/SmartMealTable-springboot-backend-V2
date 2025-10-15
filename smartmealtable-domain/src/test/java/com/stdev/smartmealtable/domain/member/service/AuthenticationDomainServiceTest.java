package com.stdev.smartmealtable.domain.member.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.AuthenticationException;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.domain.member.entity.MemberAuthentication;
import com.stdev.smartmealtable.domain.member.repository.MemberAuthenticationRepository;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

/**
 * AuthenticationDomainService 단위 테스트 (BDD Mockist 스타일)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationDomainService 테스트")
class AuthenticationDomainServiceTest {

    @Mock
    private MemberAuthenticationRepository memberAuthenticationRepository;

    @InjectMocks
    private AuthenticationDomainService authenticationDomainService;

    @Nested
    @DisplayName("authenticate 메서드는")
    class Describe_authenticate {

        @Nested
        @DisplayName("올바른 이메일과 비밀번호가 주어지면")
        class Context_with_valid_credentials {

            @Test
            @DisplayName("인증에 성공하고 MemberAuthentication을 반환한다")
            void it_authenticates_successfully() {
                // Given
                String email = "test@example.com";
                String password = "password123!";
                String hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray());

                MemberAuthentication authentication = MemberAuthentication.reconstitute(
                        1L,
                        1L,
                        email,
                        hashedPassword,
                        0,
                        LocalDateTime.now(),
                        LocalDateTime.now().plusDays(90),
                        "홍길동",
                        null,
                        LocalDateTime.now()
                );

                given(memberAuthenticationRepository.findByEmail(email))
                        .willReturn(Optional.of(authentication));

                // When
                MemberAuthentication result = authenticationDomainService.authenticate(email, password);

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getEmail()).isEqualTo(email);
                then(memberAuthenticationRepository).should(times(1)).findByEmail(email);
            }
        }

        @Nested
        @DisplayName("존재하지 않는 이메일이 주어지면")
        class Context_with_non_existing_email {

            @Test
            @DisplayName("INVALID_CREDENTIALS 예외를 발생시킨다")
            void it_throws_invalid_credentials_exception() {
                // Given
                String email = "nonexisting@example.com";
                String password = "password123!";

                given(memberAuthenticationRepository.findByEmail(email))
                        .willReturn(Optional.empty());

                // When & Then
                assertThatThrownBy(() -> authenticationDomainService.authenticate(email, password))
                        .isInstanceOf(AuthenticationException.class)
                        .hasFieldOrPropertyWithValue("errorType", ErrorType.INVALID_CREDENTIALS);

                then(memberAuthenticationRepository).should(times(1)).findByEmail(email);
            }
        }

        @Nested
        @DisplayName("잘못된 비밀번호가 주어지면")
        class Context_with_wrong_password {

            @Test
            @DisplayName("INVALID_CREDENTIALS 예외를 발생시킨다")
            void it_throws_invalid_credentials_exception() {
                // Given
                String email = "test@example.com";
                String correctPassword = "password123!";
                String wrongPassword = "wrongpassword";
                String hashedPassword = BCrypt.withDefaults().hashToString(12, correctPassword.toCharArray());

                MemberAuthentication authentication = MemberAuthentication.reconstitute(
                        1L,
                        1L,
                        email,
                        hashedPassword,
                        0,
                        LocalDateTime.now(),
                        LocalDateTime.now().plusDays(90),
                        "홍길동",
                        null,
                        LocalDateTime.now()
                );

                given(memberAuthenticationRepository.findByEmail(email))
                        .willReturn(Optional.of(authentication));

                // When & Then
                assertThatThrownBy(() -> authenticationDomainService.authenticate(email, wrongPassword))
                        .isInstanceOf(AuthenticationException.class)
                        .hasFieldOrPropertyWithValue("errorType", ErrorType.INVALID_CREDENTIALS);

                then(memberAuthenticationRepository).should(times(1)).findByEmail(email);
            }
        }
    }

    @Nested
    @DisplayName("getAuthenticationByMemberId 메서드는")
    class Describe_getAuthenticationByMemberId {

        @Nested
        @DisplayName("존재하는 회원 ID가 주어지면")
        class Context_with_existing_member_id {

            @Test
            @DisplayName("인증 정보를 반환한다")
            void it_returns_authentication() {
                // Given
                Long memberId = 1L;
                MemberAuthentication authentication = MemberAuthentication.reconstitute(
                        1L,
                        memberId,
                        "test@example.com",
                        "hashedPassword",
                        0,
                        LocalDateTime.now(),
                        LocalDateTime.now().plusDays(90),
                        "홍길동",
                        null,
                        LocalDateTime.now()
                );

                given(memberAuthenticationRepository.findByMemberId(memberId))
                        .willReturn(Optional.of(authentication));

                // When
                MemberAuthentication result = authenticationDomainService.getAuthenticationByMemberId(memberId);

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getMemberId()).isEqualTo(memberId);
                then(memberAuthenticationRepository).should(times(1)).findByMemberId(memberId);
            }
        }

        @Nested
        @DisplayName("존재하지 않는 회원 ID가 주어지면")
        class Context_with_non_existing_member_id {

            @Test
            @DisplayName("MEMBER_NOT_FOUND 예외를 발생시킨다")
            void it_throws_member_not_found_exception() {
                // Given
                Long memberId = 999L;
                given(memberAuthenticationRepository.findByMemberId(memberId))
                        .willReturn(Optional.empty());

                // When & Then
                assertThatThrownBy(() -> authenticationDomainService.getAuthenticationByMemberId(memberId))
                        .isInstanceOf(AuthenticationException.class)
                        .hasFieldOrPropertyWithValue("errorType", ErrorType.MEMBER_NOT_FOUND);

                then(memberAuthenticationRepository).should(times(1)).findByMemberId(memberId);
            }
        }
    }

    @Nested
    @DisplayName("isValidPasswordFormat 메서드는")
    class Describe_isValidPasswordFormat {

        @Nested
        @DisplayName("유효한 비밀번호 형식이 주어지면")
        class Context_with_valid_format {

            @Test
            @DisplayName("true를 반환한다")
            void it_returns_true() {
                // Given
                String validPassword = "Password123!";

                // When
                boolean result = authenticationDomainService.isValidPasswordFormat(validPassword);

                // Then
                assertThat(result).isTrue();
            }
        }

        @Nested
        @DisplayName("너무 짧은 비밀번호가 주어지면")
        class Context_with_too_short_password {

            @Test
            @DisplayName("false를 반환한다")
            void it_returns_false() {
                // Given
                String shortPassword = "Pwd1!";

                // When
                boolean result = authenticationDomainService.isValidPasswordFormat(shortPassword);

                // Then
                assertThat(result).isFalse();
            }
        }

        @Nested
        @DisplayName("특수문자가 없는 비밀번호가 주어지면")
        class Context_without_special_character {

            @Test
            @DisplayName("false를 반환한다")
            void it_returns_false() {
                // Given
                String noSpecialChar = "Password123";

                // When
                boolean result = authenticationDomainService.isValidPasswordFormat(noSpecialChar);

                // Then
                assertThat(result).isFalse();
            }
        }

        @Nested
        @DisplayName("숫자가 없는 비밀번호가 주어지면")
        class Context_without_number {

            @Test
            @DisplayName("false를 반환한다")
            void it_returns_false() {
                // Given
                String noNumber = "Password!@#";

                // When
                boolean result = authenticationDomainService.isValidPasswordFormat(noNumber);

                // Then
                assertThat(result).isFalse();
            }
        }

        @Nested
        @DisplayName("문자가 없는 비밀번호가 주어지면")
        class Context_without_letter {

            @Test
            @DisplayName("false를 반환한다")
            void it_returns_false() {
                // Given
                String noLetter = "123456!@#";

                // When
                boolean result = authenticationDomainService.isValidPasswordFormat(noLetter);

                // Then
                assertThat(result).isFalse();
            }
        }
    }

    @Nested
    @DisplayName("changePassword 메서드는")
    class Describe_changePassword {

        @Nested
        @DisplayName("유효한 현재 비밀번호와 새 비밀번호가 주어지면")
        class Context_with_valid_passwords {

            @Test
            @DisplayName("비밀번호를 변경한다")
            void it_changes_password() {
                // Given
                Long memberId = 1L;
                String currentPassword = "OldPassword1!";
                String newPassword = "NewPassword2@";
                String hashedCurrentPassword = BCrypt.withDefaults().hashToString(12, currentPassword.toCharArray());

                MemberAuthentication authentication = MemberAuthentication.reconstitute(
                        1L,
                        memberId,
                        "test@example.com",
                        hashedCurrentPassword,
                        0,
                        LocalDateTime.now(),
                        LocalDateTime.now().plusDays(90),
                        "홍길동",
                        null,
                        LocalDateTime.now()
                );

                given(memberAuthenticationRepository.findByMemberId(memberId))
                        .willReturn(Optional.of(authentication));

                // When
                authenticationDomainService.changePassword(memberId, currentPassword, newPassword);

                // Then
                then(memberAuthenticationRepository).should(times(1)).findByMemberId(memberId);
            }
        }

        @Nested
        @DisplayName("잘못된 현재 비밀번호가 주어지면")
        class Context_with_wrong_current_password {

            @Test
            @DisplayName("INVALID_CURRENT_PASSWORD 예외를 발생시킨다")
            void it_throws_invalid_current_password_exception() {
                // Given
                Long memberId = 1L;
                String correctCurrentPassword = "OldPassword1!";
                String wrongCurrentPassword = "WrongPassword1!";
                String newPassword = "NewPassword2@";
                String hashedPassword = BCrypt.withDefaults().hashToString(12, correctCurrentPassword.toCharArray());

                MemberAuthentication authentication = MemberAuthentication.reconstitute(
                        1L,
                        memberId,
                        "test@example.com",
                        hashedPassword,
                        0,
                        LocalDateTime.now(),
                        LocalDateTime.now().plusDays(90),
                        "홍길동",
                        null,
                        LocalDateTime.now()
                );

                given(memberAuthenticationRepository.findByMemberId(memberId))
                        .willReturn(Optional.of(authentication));

                // When & Then
                assertThatThrownBy(() -> authenticationDomainService.changePassword(
                        memberId, wrongCurrentPassword, newPassword
                ))
                        .isInstanceOf(AuthenticationException.class)
                        .hasFieldOrPropertyWithValue("errorType", ErrorType.INVALID_CURRENT_PASSWORD);

                then(memberAuthenticationRepository).should(times(1)).findByMemberId(memberId);
            }
        }

        @Nested
        @DisplayName("유효하지 않은 형식의 새 비밀번호가 주어지면")
        class Context_with_invalid_new_password_format {

            @Test
            @DisplayName("INVALID_PASSWORD_FORMAT 예외를 발생시킨다")
            void it_throws_invalid_password_format_exception() {
                // Given
                Long memberId = 1L;
                String currentPassword = "OldPassword1!";
                String invalidNewPassword = "weak";  // 형식 검증 실패
                String hashedPassword = BCrypt.withDefaults().hashToString(12, currentPassword.toCharArray());

                MemberAuthentication authentication = MemberAuthentication.reconstitute(
                        1L,
                        memberId,
                        "test@example.com",
                        hashedPassword,
                        0,
                        LocalDateTime.now(),
                        LocalDateTime.now().plusDays(90),
                        "홍길동",
                        null,
                        LocalDateTime.now()
                );

                given(memberAuthenticationRepository.findByMemberId(memberId))
                        .willReturn(Optional.of(authentication));

                // When & Then
                assertThatThrownBy(() -> authenticationDomainService.changePassword(
                        memberId, currentPassword, invalidNewPassword
                ))
                        .isInstanceOf(BusinessException.class)
                        .hasFieldOrPropertyWithValue("errorType", ErrorType.INVALID_PASSWORD_FORMAT);

                then(memberAuthenticationRepository).should(times(1)).findByMemberId(memberId);
            }
        }
    }
}

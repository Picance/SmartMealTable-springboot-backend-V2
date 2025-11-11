package com.stdev.smartmealtable.domain.member.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.domain.member.entity.Member;
import com.stdev.smartmealtable.domain.member.entity.MemberAuthentication;
import com.stdev.smartmealtable.domain.member.entity.RecommendationType;
import com.stdev.smartmealtable.domain.member.repository.MemberAuthenticationRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberRepository;
import com.stdev.smartmealtable.domain.policy.entity.Policy;
import com.stdev.smartmealtable.domain.policy.entity.PolicyAgreement;
import com.stdev.smartmealtable.domain.policy.entity.PolicyType;
import com.stdev.smartmealtable.domain.policy.repository.PolicyAgreementRepository;
import com.stdev.smartmealtable.domain.policy.repository.PolicyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

/**
 * MemberDomainService 단위 테스트 (BDD Mockist 스타일)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MemberDomainService 테스트")
class MemberDomainServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberAuthenticationRepository memberAuthenticationRepository;

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private PolicyAgreementRepository policyAgreementRepository;

    @InjectMocks
    private MemberDomainService memberDomainService;

    @Nested
    @DisplayName("isEmailDuplicated 메서드는")
    class Describe_isEmailDuplicated {

        @Nested
        @DisplayName("이메일이 이미 존재하는 경우")
        class Context_with_existing_email {

            @Test
            @DisplayName("true를 반환한다")
            void it_returns_true() {
                // Given
                String email = "test@example.com";
                given(memberAuthenticationRepository.existsByEmail(email)).willReturn(true);

                // When
                boolean result = memberDomainService.isEmailDuplicated(email);

                // Then
                assertThat(result).isTrue();
                then(memberAuthenticationRepository).should(times(1)).existsByEmail(email);
            }
        }

        @Nested
        @DisplayName("이메일이 존재하지 않는 경우")
        class Context_with_non_existing_email {

            @Test
            @DisplayName("false를 반환한다")
            void it_returns_false() {
                // Given
                String email = "new@example.com";
                given(memberAuthenticationRepository.existsByEmail(email)).willReturn(false);

                // When
                boolean result = memberDomainService.isEmailDuplicated(email);

                // Then
                assertThat(result).isFalse();
                then(memberAuthenticationRepository).should(times(1)).existsByEmail(email);
            }
        }
    }

    @Nested
    @DisplayName("createMemberWithAuthentication 메서드는")
    class Describe_createMemberWithAuthentication {

        @Nested
        @DisplayName("유효한 정보가 주어지면")
        class Context_with_valid_info {

            @Test
            @DisplayName("회원과 인증 정보를 생성한다")
            void it_creates_member_and_authentication() {
                // Given
                String name = "홍길동";
                String email = "hong@example.com";
                String password = "password123!";

                Member savedMember = Member.reconstitute(
                        1L,
                        null,
                        name,
                        null,
                        RecommendationType.BALANCED
                );

                String hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray());
                MemberAuthentication savedAuth = MemberAuthentication.reconstitute(
                        1L,
                        1L,
                        email,
                        hashedPassword,
                        0,
                        LocalDateTime.now(),
                        LocalDateTime.now().plusDays(90),
                        name,
                        null,
                        LocalDateTime.now()
                );

                given(memberAuthenticationRepository.existsByEmail(email)).willReturn(false);
                given(memberRepository.save(any(Member.class))).willReturn(savedMember);
                given(memberAuthenticationRepository.save(any(MemberAuthentication.class))).willReturn(savedAuth);

                // When
                MemberAuthentication result = memberDomainService.createMemberWithAuthentication(name, email, password);

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getEmail()).isEqualTo(email);
                assertThat(result.getName()).isEqualTo(name);
                assertThat(result.getMemberId()).isEqualTo(1L);
                
                then(memberAuthenticationRepository).should(times(1)).existsByEmail(email);
                then(memberRepository).should(times(1)).save(any(Member.class));
                then(memberAuthenticationRepository).should(times(1)).save(any(MemberAuthentication.class));
            }
        }

        @Nested
        @DisplayName("중복된 이메일이 주어지면")
        class Context_with_duplicate_email {

            @Test
            @DisplayName("DUPLICATE_EMAIL 예외를 발생시킨다")
            void it_throws_duplicate_email_exception() {
                // Given
                String name = "홍길동";
                String email = "duplicate@example.com";
                String password = "password123!";

                given(memberAuthenticationRepository.existsByEmail(email)).willReturn(true);

                // When & Then
                assertThatThrownBy(() -> memberDomainService.createMemberWithAuthentication(name, email, password))
                        .isInstanceOf(BusinessException.class)
                        .hasFieldOrPropertyWithValue("errorType", ErrorType.DUPLICATE_EMAIL);

                then(memberAuthenticationRepository).should(times(1)).existsByEmail(email);
                then(memberRepository).should(times(0)).save(any(Member.class));
                then(memberAuthenticationRepository).should(times(0)).save(any(MemberAuthentication.class));
            }
        }
    }

    @Nested
    @DisplayName("getMemberById 메서드는")
    class Describe_getMemberById {

        @Nested
        @DisplayName("존재하는 회원 ID가 주어지면")
        class Context_with_existing_member_id {

            @Test
            @DisplayName("회원 정보를 반환한다")
            void it_returns_member() {
                // Given
                Long memberId = 1L;
                Member member = Member.reconstitute(
                        memberId,
                        1L,
                        "홍길동",
                        null,
                        RecommendationType.BALANCED
                );

                given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

                // When
                Member result = memberDomainService.getMemberById(memberId);

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getMemberId()).isEqualTo(memberId);
                assertThat(result.getNickname()).isEqualTo("홍길동");
                
                then(memberRepository).should(times(1)).findById(memberId);
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
                given(memberRepository.findById(memberId)).willReturn(Optional.empty());

                // When & Then
                assertThatThrownBy(() -> memberDomainService.getMemberById(memberId))
                        .isInstanceOf(BusinessException.class)
                        .hasFieldOrPropertyWithValue("errorType", ErrorType.MEMBER_NOT_FOUND);

                then(memberRepository).should(times(1)).findById(memberId);
            }
        }
    }

    @Nested
    @DisplayName("isOnboardingComplete 메서드는")
    class Describe_isOnboardingComplete {

        @Test
        @DisplayName("모든 필수 약관에 동의했다면 true를 반환한다")
        void it_returns_true_when_all_mandatory_policies_are_agreed() {
            // Given
            Long memberId = 1L;
            MemberAuthentication authentication = MemberAuthentication.reconstitute(
                    10L,
                    memberId,
                    "test@example.com",
                    null,
                    0,
                    LocalDateTime.now(),
                    LocalDateTime.now().plusDays(90),
                    "홍길동",
                    null,
                    LocalDateTime.now()
            );

            Policy mandatoryPolicy = Policy.reconstitute(
                    100L,
                    "필수 이용약관",
                    "내용",
                    PolicyType.REQUIRED,
                    "v1.0",
                    true,
                    true
            );
            Policy optionalPolicy = Policy.reconstitute(
                    200L,
                    "선택 약관",
                    "내용",
                    PolicyType.OPTIONAL,
                    "v1.0",
                    false,
                    true
            );

            PolicyAgreement agreement = PolicyAgreement.reconstitute(
                    1000L,
                    mandatoryPolicy.getPolicyId(),
                    authentication.getMemberAuthenticationId(),
                    true,
                    LocalDateTime.now()
            );

            given(memberAuthenticationRepository.findByMemberId(memberId))
                    .willReturn(Optional.of(authentication));
            given(policyRepository.findAllActive())
                    .willReturn(List.of(mandatoryPolicy, optionalPolicy));
            given(policyAgreementRepository.findByMemberAuthenticationId(authentication.getMemberAuthenticationId()))
                    .willReturn(List.of(agreement));

            // When
            boolean result = memberDomainService.isOnboardingComplete(memberId);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("필수 약관에 미동의가 있으면 false를 반환한다")
        void it_returns_false_when_missing_mandatory_agreement() {
            // Given
            Long memberId = 1L;
            MemberAuthentication authentication = MemberAuthentication.reconstitute(
                    10L,
                    memberId,
                    "test@example.com",
                    null,
                    0,
                    LocalDateTime.now(),
                    LocalDateTime.now().plusDays(90),
                    "홍길동",
                    null,
                    LocalDateTime.now()
            );

            Policy mandatoryPolicy = Policy.reconstitute(
                    100L,
                    "필수 이용약관",
                    "내용",
                    PolicyType.REQUIRED,
                    "v1.0",
                    true,
                    true
            );

            given(memberAuthenticationRepository.findByMemberId(memberId))
                    .willReturn(Optional.of(authentication));
            given(policyRepository.findAllActive())
                    .willReturn(List.of(mandatoryPolicy));
            given(policyAgreementRepository.findByMemberAuthenticationId(authentication.getMemberAuthenticationId()))
                    .willReturn(List.of());

            // When
            boolean result = memberDomainService.isOnboardingComplete(memberId);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("회원 인증 정보를 찾을 수 없으면 MEMBER_NOT_FOUND 예외를 던진다")
        void it_throws_when_member_authentication_not_found() {
            // Given
            Long memberId = 999L;
            given(memberAuthenticationRepository.findByMemberId(memberId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> memberDomainService.isOnboardingComplete(memberId))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorType", ErrorType.MEMBER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("withdrawMember 메서드는")
    class Describe_withdrawMember {

        @Nested
        @DisplayName("유효한 비밀번호가 주어지면")
        class Context_with_valid_password {

            @Test
            @DisplayName("회원 탈퇴를 처리한다")
            void it_withdraws_member() {
                // Given
                Long memberId = 1L;
                String password = "password123!";
                String reason = "서비스 불만족";

                String hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray());
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

                // When
                memberDomainService.withdrawMember(memberId, password, reason);

                // Then
                then(memberAuthenticationRepository).should(times(1)).findByMemberId(memberId);
                assertThat(authentication.isWithdrawn()).isTrue();
            }

            @Test
            @DisplayName("탈퇴 사유 없이 회원 탈퇴를 처리한다")
            void it_withdraws_member_without_reason() {
                // Given
                Long memberId = 1L;
                String password = "password123!";

                String hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray());
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

                // When
                memberDomainService.withdrawMember(memberId, password, null);

                // Then
                then(memberAuthenticationRepository).should(times(1)).findByMemberId(memberId);
                assertThat(authentication.isWithdrawn()).isTrue();
            }
        }

        @Nested
        @DisplayName("잘못된 비밀번호가 주어지면")
        class Context_with_invalid_password {

            @Test
            @DisplayName("INVALID_CURRENT_PASSWORD 예외를 발생시킨다")
            void it_throws_invalid_password_exception() {
                // Given
                Long memberId = 1L;
                String correctPassword = "password123!";
                String wrongPassword = "wrongpassword";
                String reason = "서비스 불만족";

                String hashedPassword = BCrypt.withDefaults().hashToString(12, correctPassword.toCharArray());
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
                assertThatThrownBy(() -> memberDomainService.withdrawMember(memberId, wrongPassword, reason))
                        .isInstanceOf(BusinessException.class)
                        .hasFieldOrPropertyWithValue("errorType", ErrorType.INVALID_CURRENT_PASSWORD);

                then(memberAuthenticationRepository).should(times(1)).findByMemberId(memberId);
                assertThat(authentication.isWithdrawn()).isFalse();
            }
        }

        @Nested
        @DisplayName("존재하지 않는 회원 ID가 주어지면")
        class Context_with_non_existing_member {

            @Test
            @DisplayName("MEMBER_NOT_FOUND 예외를 발생시킨다")
            void it_throws_member_not_found_exception() {
                // Given
                Long memberId = 999L;
                String password = "password123!";
                String reason = "서비스 불만족";

                given(memberAuthenticationRepository.findByMemberId(memberId))
                        .willReturn(Optional.empty());

                // When & Then
                assertThatThrownBy(() -> memberDomainService.withdrawMember(memberId, password, reason))
                        .isInstanceOf(BusinessException.class)
                        .hasFieldOrPropertyWithValue("errorType", ErrorType.MEMBER_NOT_FOUND);

                then(memberAuthenticationRepository).should(times(1)).findByMemberId(memberId);
            }
        }
    }
}

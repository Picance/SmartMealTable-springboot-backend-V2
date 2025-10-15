package com.stdev.smartmealtable.api.member.service;

import com.stdev.smartmealtable.api.member.dto.response.MemberProfileResponse;
import com.stdev.smartmealtable.api.member.dto.response.UpdateProfileResponse;
import com.stdev.smartmealtable.api.member.service.dto.UpdateProfileServiceRequest;
import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.domain.member.entity.*;
import com.stdev.smartmealtable.domain.member.repository.GroupRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberAuthenticationRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberRepository;
import com.stdev.smartmealtable.domain.member.repository.SocialAccountRepository;
import com.stdev.smartmealtable.domain.member.service.ProfileDomainService;
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
 * MemberProfileService 단위 테스트 (BDD Mockist 스타일)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MemberProfileService 테스트")
class MemberProfileServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberAuthenticationRepository memberAuthenticationRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private SocialAccountRepository socialAccountRepository;

    @Mock
    private ProfileDomainService profileDomainService;

    @InjectMocks
    private MemberProfileService memberProfileService;

    @Nested
    @DisplayName("getProfile 메서드는")
    class Describe_getProfile {

        @Nested
        @DisplayName("존재하는 회원 ID가 주어지면")
        class Context_with_existing_member_id {

            @Test
            @DisplayName("회원 프로필 정보를 반환한다")
            void it_returns_member_profile() {
                // Given
                Long memberId = 1L;
                Long groupId = 2L;

                Member member = Member.reconstitute(memberId, groupId, "테스트유저", RecommendationType.BALANCED);

                MemberAuthentication auth = MemberAuthentication.reconstitute(
                        1L, // memberAuthenticationId
                        memberId,
                        "test@example.com",
                        "hashedPassword",
                        0,
                        LocalDateTime.now().minusDays(10),
                        LocalDateTime.now().plusDays(80),
                        "홍길동",
                        null,
                        LocalDateTime.now().minusDays(30)
                );

                Group group = Group.reconstitute(groupId, "대학생", GroupType.UNIVERSITY, "서울");

                SocialAccount kakaoAccount = SocialAccount.create(
                        auth.getMemberAuthenticationId(),
                        SocialProvider.KAKAO,
                        "kakao123",
                        "카카오유저",
                        "kakao@example.com",
                        "https://kakao.profile.jpg",
                        LocalDateTime.now()
                );

                given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
                given(memberAuthenticationRepository.findByMemberId(memberId))
                        .willReturn(Optional.of(auth));
                given(groupRepository.findById(groupId)).willReturn(Optional.of(group));
                given(socialAccountRepository.findByMemberAuthenticationId(any()))
                        .willReturn(List.of(kakaoAccount));

                // When
                MemberProfileResponse response = memberProfileService.getProfile(memberId);

                // Then
                assertThat(response).isNotNull();
                assertThat(response.getMemberId()).isEqualTo(memberId);
                assertThat(response.getNickname()).isEqualTo("테스트유저");
                assertThat(response.getEmail()).isEqualTo("test@example.com");
                assertThat(response.getName()).isEqualTo("홍길동");
                assertThat(response.getRecommendationType()).isEqualTo(RecommendationType.BALANCED);
                assertThat(response.getGroup()).isNotNull();
                assertThat(response.getSocialAccounts()).hasSize(1);

                then(memberRepository).should(times(1)).findById(memberId);
                then(memberAuthenticationRepository).should(times(1)).findByMemberId(memberId);
                then(groupRepository).should(times(1)).findById(groupId);
                then(socialAccountRepository).should(times(1))
                        .findByMemberAuthenticationId(any());
            }
        }

        @Nested
        @DisplayName("그룹이 없는 회원 ID가 주어지면")
        class Context_with_member_without_group {

            @Test
            @DisplayName("그룹 정보 없이 프로필을 반환한다")
            void it_returns_profile_without_group() {
                // Given
                Long memberId = 1L;

                Member member = Member.reconstitute(memberId, null, "테스트유저", RecommendationType.SAVER);

                MemberAuthentication auth = MemberAuthentication.reconstitute(
                        1L, // memberAuthenticationId
                        memberId,
                        "test@example.com",
                        "hashedPassword",
                        0,
                        LocalDateTime.now().minusDays(10),
                        LocalDateTime.now().plusDays(80),
                        "홍길동",
                        null,
                        LocalDateTime.now().minusDays(30)
                );

                given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
                given(memberAuthenticationRepository.findByMemberId(memberId))
                        .willReturn(Optional.of(auth));
                given(socialAccountRepository.findByMemberAuthenticationId(any()))
                        .willReturn(List.of());

                // When
                MemberProfileResponse response = memberProfileService.getProfile(memberId);

                // Then
                assertThat(response).isNotNull();
                assertThat(response.getMemberId()).isEqualTo(memberId);
                assertThat(response.getGroup()).isNull();
                assertThat(response.getSocialAccounts()).isEmpty();

                then(memberRepository).should(times(1)).findById(memberId);
                then(memberAuthenticationRepository).should(times(1)).findByMemberId(memberId);
                then(groupRepository).should(times(0)).findById(any());
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
                assertThatThrownBy(() -> memberProfileService.getProfile(memberId))
                        .isInstanceOf(BusinessException.class)
                        .hasFieldOrPropertyWithValue("errorType", ErrorType.MEMBER_NOT_FOUND);

                then(memberRepository).should(times(1)).findById(memberId);
                then(memberAuthenticationRepository).should(times(0)).findByMemberId(any());
            }
        }

        @Nested
        @DisplayName("회원은 존재하지만 인증 정보가 없으면")
        class Context_with_member_but_no_authentication {

            @Test
            @DisplayName("MEMBER_NOT_FOUND 예외를 발생시킨다")
            void it_throws_member_not_found_exception() {
                // Given
                Long memberId = 1L;

                Member member = Member.reconstitute(memberId, null, "테스트유저", RecommendationType.ADVENTURER);

                given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
                given(memberAuthenticationRepository.findByMemberId(memberId))
                        .willReturn(Optional.empty());

                // When & Then
                assertThatThrownBy(() -> memberProfileService.getProfile(memberId))
                        .isInstanceOf(BusinessException.class)
                        .hasFieldOrPropertyWithValue("errorType", ErrorType.MEMBER_NOT_FOUND);

                then(memberRepository).should(times(1)).findById(memberId);
                then(memberAuthenticationRepository).should(times(1)).findByMemberId(memberId);
            }
        }
    }

    @Nested
    @DisplayName("updateProfile 메서드는")
    class Describe_updateProfile {

        @Nested
        @DisplayName("유효한 프로필 수정 요청이 주어지면")
        class Context_with_valid_update_request {

            @Test
            @DisplayName("프로필을 수정하고 결과를 반환한다")
            void it_updates_and_returns_profile() {
                // Given
                Long memberId = 1L;
                String nickname = "새닉네임";
                Long groupId = 2L;

                UpdateProfileServiceRequest request = UpdateProfileServiceRequest.builder()
                        .memberId(memberId)
                        .nickname(nickname)
                        .groupId(groupId)
                        .build();

                Member updatedMember = Member.reconstitute(memberId, groupId, nickname, RecommendationType.BALANCED);

                Group group = Group.reconstitute(groupId, "대학생", GroupType.UNIVERSITY, "서울");

                given(profileDomainService.updateProfile(memberId, nickname, groupId))
                        .willReturn(updatedMember);
                given(groupRepository.findById(groupId)).willReturn(Optional.of(group));

                // When
                UpdateProfileResponse response = memberProfileService.updateProfile(request);

                // Then
                assertThat(response).isNotNull();
                assertThat(response.getMemberId()).isEqualTo(memberId);
                assertThat(response.getNickname()).isEqualTo(nickname);
                assertThat(response.getGroup()).isNotNull();
                assertThat(response.getGroup().getGroupId()).isEqualTo(groupId);

                then(profileDomainService).should(times(1))
                        .updateProfile(memberId, nickname, groupId);
                then(groupRepository).should(times(1)).findById(groupId);
            }
        }

        @Nested
        @DisplayName("그룹이 존재하지 않으면")
        class Context_with_non_existing_group {

            @Test
            @DisplayName("GROUP_NOT_FOUND 예외를 발생시킨다")
            void it_throws_group_not_found_exception() {
                // Given
                Long memberId = 1L;
                String nickname = "새닉네임";
                Long groupId = 999L;

                UpdateProfileServiceRequest request = UpdateProfileServiceRequest.builder()
                        .memberId(memberId)
                        .nickname(nickname)
                        .groupId(groupId)
                        .build();

                Member updatedMember = Member.reconstitute(memberId, groupId, nickname, RecommendationType.SAVER);

                given(profileDomainService.updateProfile(memberId, nickname, groupId))
                        .willReturn(updatedMember);
                given(groupRepository.findById(groupId)).willReturn(Optional.empty());

                // When & Then
                assertThatThrownBy(() -> memberProfileService.updateProfile(request))
                        .isInstanceOf(BusinessException.class)
                        .hasFieldOrPropertyWithValue("errorType", ErrorType.GROUP_NOT_FOUND);

                then(profileDomainService).should(times(1))
                        .updateProfile(memberId, nickname, groupId);
                then(groupRepository).should(times(1)).findById(groupId);
            }
        }
    }
}

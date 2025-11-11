package com.stdev.smartmealtable.domain.member.service;

import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.domain.common.vo.Address;
import com.stdev.smartmealtable.domain.member.entity.Group;
import com.stdev.smartmealtable.domain.member.entity.GroupType;
import com.stdev.smartmealtable.domain.member.entity.Member;
import com.stdev.smartmealtable.domain.member.entity.RecommendationType;
import com.stdev.smartmealtable.domain.member.repository.GroupRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

/**
 * ProfileDomainService 단위 테스트 (BDD Mockist 스타일)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProfileDomainService 테스트")
class ProfileDomainServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private GroupRepository groupRepository;

    @InjectMocks
    private ProfileDomainService profileDomainService;

    @Nested
    @DisplayName("isNicknameDuplicated(nickname, memberId) 메서드는")
    class Describe_isNicknameDuplicated_with_memberId {

        @Nested
        @DisplayName("닉네임이 다른 회원에게 사용되고 있으면")
        class Context_with_nickname_used_by_others {

            @Test
            @DisplayName("true를 반환한다")
            void it_returns_true() {
                // Given
                String nickname = "중복닉네임";
                Long memberId = 1L;

                given(memberRepository.existsByNicknameExcludingMemberId(nickname, memberId))
                        .willReturn(true);

                // When
                boolean result = profileDomainService.isNicknameDuplicated(nickname, memberId);

                // Then
                assertThat(result).isTrue();
                then(memberRepository).should(times(1))
                        .existsByNicknameExcludingMemberId(nickname, memberId);
            }
        }

        @Nested
        @DisplayName("닉네임이 사용 가능하면")
        class Context_with_available_nickname {

            @Test
            @DisplayName("false를 반환한다")
            void it_returns_false() {
                // Given
                String nickname = "사용가능닉네임";
                Long memberId = 1L;

                given(memberRepository.existsByNicknameExcludingMemberId(nickname, memberId))
                        .willReturn(false);

                // When
                boolean result = profileDomainService.isNicknameDuplicated(nickname, memberId);

                // Then
                assertThat(result).isFalse();
                then(memberRepository).should(times(1))
                        .existsByNicknameExcludingMemberId(nickname, memberId);
            }
        }
    }

    @Nested
    @DisplayName("isNicknameDuplicated(nickname) 메서드는")
    class Describe_isNicknameDuplicated_without_memberId {

        @Nested
        @DisplayName("닉네임이 이미 사용되고 있으면")
        class Context_with_used_nickname {

            @Test
            @DisplayName("true를 반환한다")
            void it_returns_true() {
                // Given
                String nickname = "사용중닉네임";

                given(memberRepository.existsByNickname(nickname)).willReturn(true);

                // When
                boolean result = profileDomainService.isNicknameDuplicated(nickname);

                // Then
                assertThat(result).isTrue();
                then(memberRepository).should(times(1)).existsByNickname(nickname);
            }
        }

        @Nested
        @DisplayName("닉네임이 사용 가능하면")
        class Context_with_available_nickname {

            @Test
            @DisplayName("false를 반환한다")
            void it_returns_false() {
                // Given
                String nickname = "사용가능닉네임";

                given(memberRepository.existsByNickname(nickname)).willReturn(false);

                // When
                boolean result = profileDomainService.isNicknameDuplicated(nickname);

                // Then
                assertThat(result).isFalse();
                then(memberRepository).should(times(1)).existsByNickname(nickname);
            }
        }
    }

    @Nested
    @DisplayName("validateAndGetGroup 메서드는")
    class Describe_validateAndGetGroup {

        @Nested
        @DisplayName("존재하는 그룹 ID가 주어지면")
        class Context_with_existing_group_id {

            @Test
            @DisplayName("그룹 엔티티를 반환한다")
            void it_returns_group_entity() {
                // Given
                Long groupId = 1L;
                Address address = Address.of("대학생", null, "서울", null, null, null, null);
                Group group = Group.create("대학생", GroupType.UNIVERSITY, address);

                given(groupRepository.findById(groupId)).willReturn(Optional.of(group));

                // When
                Group result = profileDomainService.validateAndGetGroup(groupId);

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getName()).isEqualTo("대학생");
                then(groupRepository).should(times(1)).findById(groupId);
            }
        }

        @Nested
        @DisplayName("존재하지 않는 그룹 ID가 주어지면")
        class Context_with_non_existing_group_id {

            @Test
            @DisplayName("GROUP_NOT_FOUND 예외를 발생시킨다")
            void it_throws_group_not_found_exception() {
                // Given
                Long groupId = 999L;

                given(groupRepository.findById(groupId)).willReturn(Optional.empty());

                // When & Then
                assertThatThrownBy(() -> profileDomainService.validateAndGetGroup(groupId))
                        .isInstanceOf(BusinessException.class)
                        .hasFieldOrPropertyWithValue("errorType", ErrorType.GROUP_NOT_FOUND);

                then(groupRepository).should(times(1)).findById(groupId);
            }
        }
    }

    @Nested
    @DisplayName("updateProfile 메서드는")
    class Describe_updateProfile {

        @Nested
        @DisplayName("유효한 닉네임과 그룹 ID가 주어지면")
        class Context_with_valid_nickname_and_group_id {

            @Test
            @DisplayName("프로필을 업데이트하고 저장한다")
            void it_updates_and_saves_profile() {
                // Given
                Long memberId = 1L;
                String nickname = "새닉네임";
                Long groupId = 2L;

                Member member = Member.create(groupId, "구닉네임", null, RecommendationType.BALANCED);
                Address address = Address.of("대학생", null, "서울", null, null, null, null);
                Group group = Group.create("대학생", GroupType.UNIVERSITY, address);

                given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
                given(memberRepository.existsByNicknameExcludingMemberId(nickname, memberId))
                        .willReturn(false);
                given(groupRepository.findById(groupId)).willReturn(Optional.of(group));
                given(memberRepository.save(any(Member.class))).willReturn(member);

                // When
                Member result = profileDomainService.updateProfile(memberId, nickname, groupId);

                // Then
                assertThat(result).isNotNull();
                then(memberRepository).should(times(1)).findById(memberId);
                then(memberRepository).should(times(1))
                        .existsByNicknameExcludingMemberId(nickname, memberId);
                then(groupRepository).should(times(1)).findById(groupId);
                then(memberRepository).should(times(1)).save(any(Member.class));
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
                String nickname = "새닉네임";
                Long groupId = 2L;

                given(memberRepository.findById(memberId)).willReturn(Optional.empty());

                // When & Then
                assertThatThrownBy(() -> profileDomainService.updateProfile(memberId, nickname, groupId))
                        .isInstanceOf(BusinessException.class)
                        .hasFieldOrPropertyWithValue("errorType", ErrorType.MEMBER_NOT_FOUND);

                then(memberRepository).should(times(1)).findById(memberId);
                then(memberRepository).should(times(0))
                        .existsByNicknameExcludingMemberId(any(), any());
                then(memberRepository).should(times(0)).save(any(Member.class));
            }
        }

        @Nested
        @DisplayName("중복된 닉네임이 주어지면")
        class Context_with_duplicated_nickname {

            @Test
            @DisplayName("DUPLICATE_NICKNAME 예외를 발생시킨다")
            void it_throws_duplicate_nickname_exception() {
                // Given
                Long memberId = 1L;
                String nickname = "중복닉네임";
                Long groupId = 2L;

                Member member = Member.create(groupId, "구닉네임", null, RecommendationType.SAVER);

                given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
                given(memberRepository.existsByNicknameExcludingMemberId(nickname, memberId))
                        .willReturn(true);

                // When & Then
                assertThatThrownBy(() -> profileDomainService.updateProfile(memberId, nickname, groupId))
                        .isInstanceOf(BusinessException.class)
                        .hasFieldOrPropertyWithValue("errorType", ErrorType.DUPLICATE_NICKNAME);

                then(memberRepository).should(times(1)).findById(memberId);
                then(memberRepository).should(times(1))
                        .existsByNicknameExcludingMemberId(nickname, memberId);
                then(groupRepository).should(times(0)).findById(any());
                then(memberRepository).should(times(0)).save(any(Member.class));
            }
        }

        @Nested
        @DisplayName("존재하지 않는 그룹 ID가 주어지면")
        class Context_with_non_existing_group_id {

            @Test
            @DisplayName("GROUP_NOT_FOUND 예외를 발생시킨다")
            void it_throws_group_not_found_exception() {
                // Given
                Long memberId = 1L;
                String nickname = "새닉네임";
                Long groupId = 999L;

                Member member = Member.create(2L, "구닉네임", null, RecommendationType.ADVENTURER);

                given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
                given(memberRepository.existsByNicknameExcludingMemberId(nickname, memberId))
                        .willReturn(false);
                given(groupRepository.findById(groupId)).willReturn(Optional.empty());

                // When & Then
                assertThatThrownBy(() -> profileDomainService.updateProfile(memberId, nickname, groupId))
                        .isInstanceOf(BusinessException.class)
                        .hasFieldOrPropertyWithValue("errorType", ErrorType.GROUP_NOT_FOUND);

                then(memberRepository).should(times(1)).findById(memberId);
                then(memberRepository).should(times(1))
                        .existsByNicknameExcludingMemberId(nickname, memberId);
                then(groupRepository).should(times(1)).findById(groupId);
                then(memberRepository).should(times(0)).save(any(Member.class));
            }
        }
    }

    @Nested
    @DisplayName("setupOnboardingProfile 메서드는")
    class Describe_setupOnboardingProfile {

        @Nested
        @DisplayName("유효한 닉네임과 그룹 ID가 주어지면")
        class Context_with_valid_nickname_and_group_id {

            @Test
            @DisplayName("온보딩 프로필을 설정하고 저장한다")
            void it_sets_up_and_saves_onboarding_profile() {
                // Given
                Long memberId = 1L;
                String nickname = "온보딩닉네임";
                Long groupId = 2L;

                Member member = Member.create(null, "임시닉네임", null, RecommendationType.BALANCED);
                Address address = Address.of("대학생", null, "서울", null, null, null, null);
                Group group = Group.create("대학생", GroupType.UNIVERSITY, address);

                given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
                given(memberRepository.existsByNickname(nickname)).willReturn(false);
                given(groupRepository.findById(groupId)).willReturn(Optional.of(group));
                given(memberRepository.save(any(Member.class))).willReturn(member);

                // When
                Member result = profileDomainService.setupOnboardingProfile(memberId, nickname, groupId);

                // Then
                assertThat(result).isNotNull();
                then(memberRepository).should(times(1)).findById(memberId);
                then(memberRepository).should(times(1)).existsByNickname(nickname);
                then(groupRepository).should(times(1)).findById(groupId);
                then(memberRepository).should(times(1)).save(any(Member.class));
            }
        }

        @Nested
        @DisplayName("중복된 닉네임이 주어지면")
        class Context_with_duplicated_nickname {

            @Test
            @DisplayName("DUPLICATE_NICKNAME 예외를 발생시킨다")
            void it_throws_duplicate_nickname_exception() {
                // Given
                Long memberId = 1L;
                String nickname = "중복닉네임";
                Long groupId = 2L;

                Member member = Member.create(null, "임시닉네임", null, RecommendationType.SAVER);

                given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
                given(memberRepository.existsByNickname(nickname)).willReturn(true);

                // When & Then
                assertThatThrownBy(() -> profileDomainService.setupOnboardingProfile(memberId, nickname, groupId))
                        .isInstanceOf(BusinessException.class)
                        .hasFieldOrPropertyWithValue("errorType", ErrorType.DUPLICATE_NICKNAME);

                then(memberRepository).should(times(1)).findById(memberId);
                then(memberRepository).should(times(1)).existsByNickname(nickname);
                then(groupRepository).should(times(0)).findById(any());
                then(memberRepository).should(times(0)).save(any(Member.class));
            }
        }
    }

    @Nested
    @DisplayName("updateRecommendationType 메서드는")
    class Describe_updateRecommendationType {

        @Nested
        @DisplayName("유효한 회원 ID와 추천 유형이 주어지면")
        class Context_with_valid_member_id_and_recommendation_type {

            @Test
            @DisplayName("추천 유형을 업데이트하고 저장한다")
            void it_updates_and_saves_recommendation_type() {
                // Given
                Long memberId = 1L;
                RecommendationType recommendationType = RecommendationType.ADVENTURER;

                Member member = Member.create(null, "테스트유저", null, RecommendationType.BALANCED);

                given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
                given(memberRepository.save(any(Member.class))).willReturn(member);

                // When
                Member result = profileDomainService.updateRecommendationType(memberId, recommendationType);

                // Then
                assertThat(result).isNotNull();
                then(memberRepository).should(times(1)).findById(memberId);
                then(memberRepository).should(times(1)).save(any(Member.class));
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
                RecommendationType recommendationType = RecommendationType.SAVER;

                given(memberRepository.findById(memberId)).willReturn(Optional.empty());

                // When & Then
                assertThatThrownBy(() -> profileDomainService.updateRecommendationType(memberId, recommendationType))
                        .isInstanceOf(BusinessException.class)
                        .hasFieldOrPropertyWithValue("errorType", ErrorType.MEMBER_NOT_FOUND);

                then(memberRepository).should(times(1)).findById(memberId);
                then(memberRepository).should(times(0)).save(any(Member.class));
            }
        }
    }
}

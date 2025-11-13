package com.stdev.smartmealtable.api.member.service;

import com.stdev.smartmealtable.api.member.dto.response.MemberProfileResponse;
import com.stdev.smartmealtable.api.member.dto.response.UpdateProfileResponse;
import com.stdev.smartmealtable.api.member.service.dto.UpdateProfileServiceRequest;
import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.domain.member.entity.Group;
import com.stdev.smartmealtable.domain.member.entity.Member;
import com.stdev.smartmealtable.domain.member.entity.MemberAuthentication;
import com.stdev.smartmealtable.domain.member.entity.SocialAccount;
import com.stdev.smartmealtable.domain.member.repository.GroupRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberAuthenticationRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberRepository;
import com.stdev.smartmealtable.domain.member.repository.SocialAccountRepository;
import com.stdev.smartmealtable.domain.member.service.ProfileDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 회원 프로필 관리 Application Service
 * 유즈케이스 orchestration에 집중
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberProfileService {

    private final MemberRepository memberRepository;
    private final MemberAuthenticationRepository memberAuthenticationRepository;
    private final GroupRepository groupRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final ProfileDomainService profileDomainService;

    /**
     * 10.1 내 프로필 조회
     */
    public MemberProfileResponse getProfile(Long memberId) {
        // 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorType.MEMBER_NOT_FOUND));

        // 회원 인증 정보 조회
        MemberAuthentication memberAuthentication = memberAuthenticationRepository.findByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorType.MEMBER_NOT_FOUND));

        // 그룹 정보 조회
        Group group = null;
        if (member.getGroupId() != null) {
            group = groupRepository.findById(member.getGroupId())
                    .orElse(null);
        }

        // 소셜 계정 목록 조회
        List<SocialAccount> socialAccounts = socialAccountRepository
                .findByMemberAuthenticationId(memberAuthentication.getMemberAuthenticationId());

        // Response 생성
        return MemberProfileResponse.builder()
                .memberId(member.getMemberId())
                .nickname(member.getNickname())
                .email(memberAuthentication.getEmail())
                .name(memberAuthentication.getName())
                .recommendationType(member.getRecommendationType())
                .group(group != null ? MemberProfileResponse.GroupInfo.builder()
                        .groupId(group.getGroupId())
                        .name(group.getName())
                        .type(group.getType())
                        .build() : null)
                .socialAccounts(socialAccounts.stream()
                        .map(sa -> MemberProfileResponse.SocialAccountInfo.builder()
                                .provider(sa.getProvider().name())
                                .connectedAt(sa.getConnectedAt())
                                .build())
                        .collect(Collectors.toList()))
                .passwordExpiresAt(memberAuthentication.getPasswordExpiresAt())
                .createdAt(memberAuthentication.getRegisteredAt())
                .build();
    }

    /**
     * 10.2.1 닉네임 수정
     */
    @Transactional
    public UpdateProfileResponse updateNickname(Long memberId, String nickname) {
        Member updatedMember = profileDomainService.updateNickname(memberId, nickname);
        return buildUpdateProfileResponse(updatedMember);
    }

    /**
     * 10.2.2 프로필 수정 (그룹 변경)
     * Domain Service를 통한 비즈니스 로직 처리
     */
    @Transactional
    public UpdateProfileResponse updateProfile(UpdateProfileServiceRequest request) {
        Member updatedMember = profileDomainService.updateGroup(
                request.getMemberId(),
                request.getGroupId()
        );

        return buildUpdateProfileResponse(updatedMember);
    }

    private UpdateProfileResponse buildUpdateProfileResponse(Member member) {
        Group group = null;
        if (member.getGroupId() != null) {
            group = groupRepository.findById(member.getGroupId())
                    .orElseThrow(() -> new BusinessException(ErrorType.GROUP_NOT_FOUND));
        }

        return UpdateProfileResponse.builder()
                .memberId(member.getMemberId())
                .nickname(member.getNickname())
                .group(group != null ? UpdateProfileResponse.GroupInfo.builder()
                        .groupId(group.getGroupId())
                        .name(group.getName())
                        .type(group.getType())
                        .build() : null)
                .updatedAt(LocalDateTime.now())
                .build();
    }
}

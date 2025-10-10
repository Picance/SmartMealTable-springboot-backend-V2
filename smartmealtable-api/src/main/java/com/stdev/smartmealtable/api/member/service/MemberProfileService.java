package com.stdev.smartmealtable.api.member.service;

import com.stdev.smartmealtable.api.member.dto.response.MemberProfileResponse;
import com.stdev.smartmealtable.api.member.dto.response.UpdateProfileResponse;
import com.stdev.smartmealtable.api.member.service.dto.UpdateProfileServiceRequest;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.domain.member.entity.*;
import com.stdev.smartmealtable.domain.member.repository.GroupRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberAuthenticationRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberRepository;
import com.stdev.smartmealtable.domain.member.repository.SocialAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 회원 프로필 관리 Service
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberProfileService {

    private final MemberRepository memberRepository;
    private final MemberAuthenticationRepository memberAuthenticationRepository;
    private final GroupRepository groupRepository;
    private final SocialAccountRepository socialAccountRepository;

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
     * 10.2 프로필 수정 (닉네임, 그룹)
     */
    @Transactional
    public UpdateProfileResponse updateProfile(UpdateProfileServiceRequest request) {
        // 회원 조회
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new BusinessException(ErrorType.MEMBER_NOT_FOUND));

        // 그룹 존재 확인
        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new BusinessException(ErrorType.GROUP_NOT_FOUND));

        // 닉네임 중복 확인 (자기 자신 제외)
        if (memberRepository.existsByNicknameExcludingMemberId(request.getNickname(), request.getMemberId())) {
            throw new BusinessException(ErrorType.DUPLICATE_NICKNAME);
        }

        // 도메인 로직으로 수정
        member.changeNickname(request.getNickname());
        member.changeGroup(request.getGroupId());

        // 저장
        Member updatedMember = memberRepository.save(member);

        // Response 생성
        return UpdateProfileResponse.builder()
                .memberId(updatedMember.getMemberId())
                .nickname(updatedMember.getNickname())
                .group(UpdateProfileResponse.GroupInfo.builder()
                        .groupId(group.getGroupId())
                        .name(group.getName())
                        .type(group.getType())
                        .build())
                .updatedAt(java.time.LocalDateTime.now())
                .build();
    }
}

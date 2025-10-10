package com.stdev.smartmealtable.api.onboarding.service;

import com.stdev.smartmealtable.api.onboarding.service.dto.OnboardingProfileServiceRequest;
import com.stdev.smartmealtable.api.onboarding.service.dto.OnboardingProfileServiceResponse;
import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.domain.member.entity.Group;
import com.stdev.smartmealtable.domain.member.entity.Member;
import com.stdev.smartmealtable.domain.member.repository.GroupRepository;
import com.stdev.smartmealtable.domain.member.service.ProfileDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 온보딩 - 프로필 설정 Application Service
 * 유즈케이스: 회원의 닉네임 및 소속 그룹 설정 (orchestration에 집중)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OnboardingProfileService {

    private final GroupRepository groupRepository;
    private final ProfileDomainService profileDomainService;

    public OnboardingProfileServiceResponse updateProfile(OnboardingProfileServiceRequest request) {
        log.info("온보딩 프로필 설정 시작 - memberId: {}, nickname: {}, groupId: {}",
                request.memberId(), request.nickname(), request.groupId());

        // Domain Service를 통한 온보딩 프로필 설정 (검증 + 도메인 로직 포함)
        Member updatedMember = profileDomainService.setupOnboardingProfile(
                request.memberId(),
                request.nickname(),
                request.groupId()
        );

        // 그룹 정보 조회 (응답용)
        Group group = groupRepository.findById(updatedMember.getGroupId())
                .orElseThrow(() -> new BusinessException(ErrorType.GROUP_NOT_FOUND));

        log.info("온보딩 프로필 설정 완료 - memberId: {}", updatedMember.getMemberId());

        // Response 생성
        return new OnboardingProfileServiceResponse(
                updatedMember.getMemberId(),
                updatedMember.getNickname(),
                new OnboardingProfileServiceResponse.GroupInfo(
                        group.getGroupId(),
                        group.getName(),
                        group.getType(),
                        group.getAddress()
                )
        );
    }
}

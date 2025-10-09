package com.stdev.smartmealtable.api.onboarding.service;

import com.stdev.smartmealtable.api.onboarding.service.dto.OnboardingProfileServiceRequest;
import com.stdev.smartmealtable.api.onboarding.service.dto.OnboardingProfileServiceResponse;
import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.domain.member.entity.Group;
import com.stdev.smartmealtable.domain.member.entity.Member;
import com.stdev.smartmealtable.domain.member.repository.GroupRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 온보딩 - 프로필 설정 Application Service
 * 유즈케이스: 회원의 닉네임 및 소속 그룹 설정
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OnboardingProfileService {

    private final MemberRepository memberRepository;
    private final GroupRepository groupRepository;

    public OnboardingProfileServiceResponse updateProfile(OnboardingProfileServiceRequest request) {
        log.info("온보딩 프로필 설정 시작 - memberId: {}, nickname: {}, groupId: {}",
                request.memberId(), request.nickname(), request.groupId());

        // 1. 회원 조회
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new BusinessException(ErrorType.MEMBER_NOT_FOUND));

        // 2. 닉네임 중복 검증
        if (memberRepository.existsByNickname(request.nickname())) {
            throw new BusinessException(ErrorType.DUPLICATE_NICKNAME);
        }

        // 3. 그룹 존재 여부 검증
        Group group = groupRepository.findById(request.groupId())
                .orElseThrow(() -> new BusinessException(ErrorType.GROUP_NOT_FOUND));

        // 4. 도메인 로직: 닉네임 변경
        member.changeNickname(request.nickname());

        // 5. 도메인 로직: 그룹 설정 (groupId 변경)
        // Member 엔티티에 setGroupId 메서드 추가 필요 또는 새로운 Member 객체 생성
        // 현재 구조에서는 groupId는 불변이므로, 새로운 Member 객체를 생성해야 함
        // 하지만 온보딩 시나리오에서는 groupId를 설정하는 것이므로,
        // Member 엔티티에 updateGroupId() 메서드를 추가하는 것이 합리적
        
        // 임시로 reconstitute를 사용하여 새로운 Member 생성 (리팩토링 필요)
        Member updatedMember = Member.reconstitute(
                member.getMemberId(),
                request.groupId(),
                request.nickname(),
                member.getRecommendationType()
        );

        // 6. 저장
        Member savedMember = memberRepository.save(updatedMember);

        log.info("온보딩 프로필 설정 완료 - memberId: {}", savedMember.getMemberId());

        // 7. Response 생성
        return new OnboardingProfileServiceResponse(
                savedMember.getMemberId(),
                savedMember.getNickname(),
                new OnboardingProfileServiceResponse.GroupInfo(
                        group.getGroupId(),
                        group.getName(),
                        group.getType(),
                        group.getAddress()
                )
        );
    }
}

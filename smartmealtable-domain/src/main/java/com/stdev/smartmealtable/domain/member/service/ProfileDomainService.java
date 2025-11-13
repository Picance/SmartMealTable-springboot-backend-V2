package com.stdev.smartmealtable.domain.member.service;

import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.domain.member.entity.Group;
import com.stdev.smartmealtable.domain.member.entity.Member;
import com.stdev.smartmealtable.domain.member.entity.RecommendationType;
import com.stdev.smartmealtable.domain.member.repository.GroupRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 프로필 도메인 서비스
 * 회원 프로필 관리와 관련된 핵심 비즈니스 로직을 담당
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileDomainService {

    private final MemberRepository memberRepository;
    private final GroupRepository groupRepository;

    /**
     * 닉네임 중복 검증 (자기 자신 제외)
     *
     * @param nickname 검증할 닉네임
     * @param memberId 현재 회원 ID (중복 검사에서 제외할 회원)
     * @return 중복 여부 (true: 중복, false: 사용 가능)
     */
    public boolean isNicknameDuplicated(String nickname, Long memberId) {
        return memberRepository.existsByNicknameExcludingMemberId(nickname, memberId);
    }

    /**
     * 닉네임만 중복 검증 (회원 ID 무관)
     *
     * @param nickname 검증할 닉네임
     * @return 중복 여부 (true: 중복, false: 사용 가능)
     */
    public boolean isNicknameDuplicated(String nickname) {
        return memberRepository.existsByNickname(nickname);
    }

    /**
     * 그룹 존재 여부 검증
     *
     * @param groupId 그룹 ID
     * @return Group 엔티티
     * @throws BusinessException 그룹이 존재하지 않을 경우
     */
    public Group validateAndGetGroup(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(ErrorType.GROUP_NOT_FOUND));
    }

    /**
     * 닉네임 업데이트
     *
     * @param memberId 회원 ID
     * @param nickname 새 닉네임
     * @return 업데이트된 Member 엔티티
     */
    public Member updateNickname(Long memberId, String nickname) {
        // 1. 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorType.MEMBER_NOT_FOUND));

        // 2. 닉네임 중복 검증 (자기 자신 제외)
        if (isNicknameDuplicated(nickname, memberId)) {
            throw new BusinessException(ErrorType.DUPLICATE_NICKNAME);
        }

        // 3. 도메인 로직 적용
        member.changeNickname(nickname);

        // 4. 저장 및 반환
        return memberRepository.save(member);
    }

    /**
     * 회원 프로필 업데이트 (그룹)
     * 그룹 존재 검증 + 도메인 로직 적용
     *
     * @param memberId 회원 ID
     * @param groupId  새 그룹 ID
     * @return 업데이트된 Member 엔티티
     */
    public Member updateGroup(Long memberId, Long groupId) {
        // 1. 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorType.MEMBER_NOT_FOUND));

        // 2. 그룹 존재 검증
        validateAndGetGroup(groupId);

        // 3. 도메인 로직 적용
        member.changeGroup(groupId);

        // 4. 저장 및 반환
        return memberRepository.save(member);
    }

    /**
     * 온보딩 프로필 설정 (닉네임, 그룹)
     * 닉네임 중복 검증 + 그룹 존재 검증 + 도메인 로직 적용
     *
     * @param memberId 회원 ID
     * @param nickname 새 닉네임
     * @param groupId  새 그룹 ID
     * @return 업데이트된 Member 엔티티
     */
    public Member setupOnboardingProfile(Long memberId, String nickname, Long groupId) {
        // 1. 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorType.MEMBER_NOT_FOUND));

        // 2. 닉네임 중복 검증
        if (isNicknameDuplicated(nickname)) {
            throw new BusinessException(ErrorType.DUPLICATE_NICKNAME);
        }

        // 3. 그룹 존재 검증
        validateAndGetGroup(groupId);

        // 4. 도메인 로직 적용
        member.changeNickname(nickname);
        member.changeGroup(groupId);

        // 5. 저장 및 반환
        return memberRepository.save(member);
    }

    /**
     * 추천 유형 업데이트
     *
     * @param memberId 회원 ID
     * @param recommendationType 새 추천 유형
     * @return 업데이트된 Member 엔티티
     */
    public Member updateRecommendationType(Long memberId, RecommendationType recommendationType) {
        // 1. 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorType.MEMBER_NOT_FOUND));

        // 2. 도메인 로직 적용
        member.changeRecommendationType(recommendationType);

        // 3. 저장 및 반환
        return memberRepository.save(member);
    }
}

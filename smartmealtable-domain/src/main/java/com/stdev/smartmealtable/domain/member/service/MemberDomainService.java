package com.stdev.smartmealtable.domain.member.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.domain.member.entity.Member;
import com.stdev.smartmealtable.domain.member.entity.MemberAuthentication;
import com.stdev.smartmealtable.domain.member.entity.RecommendationType;
import com.stdev.smartmealtable.domain.member.repository.MemberAuthenticationRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 회원 도메인 서비스
 * 회원 생성, 검증 등 핵심 비즈니스 로직을 담당
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberDomainService {

    private final MemberRepository memberRepository;
    private final MemberAuthenticationRepository memberAuthenticationRepository;

    /**
     * 이메일 중복 검증
     * 
     * @param email 검증할 이메일
     * @return 중복 여부 (true: 중복, false: 사용 가능)
     */
    public boolean isEmailDuplicated(String email) {
        return memberAuthenticationRepository.existsByEmail(email);
    }

    /**
     * 회원과 인증 정보 생성
     * 
     * @param name 회원 이름
     * @param email 이메일
     * @param password 비밀번호 (평문)
     * @return 생성된 MemberAuthentication (회원 정보 포함)
     */
    public MemberAuthentication createMemberWithAuthentication(String name, String email, String password) {
        // 1. 이메일 중복 검증
        if (isEmailDuplicated(email)) {
            throw new BusinessException(ErrorType.DUPLICATE_EMAIL);
        }

        // 2. Member 생성 (초기값: nickname = name, recommendationType = BALANCED)
        Member member = Member.create(
                null,  // groupId는 null (온보딩 미완료)
                name,  // 초기 nickname은 name과 동일
                RecommendationType.BALANCED  // 기본 추천 유형
        );

        // 3. Member 저장
        Member savedMember = memberRepository.save(member);

        // 4. 비밀번호 암호화
        String hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray());

        // 5. MemberAuthentication 생성
        MemberAuthentication authentication = MemberAuthentication.createEmailAuth(
                savedMember.getMemberId(),
                email,
                hashedPassword,
                name
        );

        // 6. MemberAuthentication 저장
        return memberAuthenticationRepository.save(authentication);
    }

    /**
     * 회원 ID로 회원 조회
     * 
     * @param memberId 회원 ID
     * @return Member 엔티티
     * @throws BusinessException 회원이 존재하지 않을 경우
     */
    public Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorType.MEMBER_NOT_FOUND));
    }

    /**
     * 회원 탈퇴 (Soft Delete)
     * 
     * @param memberId 회원 ID
     * @param password 비밀번호
     * @param reason 탈퇴 사유 (선택)
     */
    public void withdrawMember(Long memberId, String password, String reason) {
        // 1. 회원 인증 정보 조회
        MemberAuthentication authentication = memberAuthenticationRepository.findByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorType.MEMBER_NOT_FOUND));

        // 2. 비밀번호 검증
        if (!authentication.verifyPassword(password)) {
            throw new BusinessException(ErrorType.INVALID_CURRENT_PASSWORD);
        }

        // 3. 회원 탈퇴 (Soft Delete)
        authentication.withdraw();
        
        // 4. 탈퇴 사유 로깅 (선택사항)
        if (reason != null && !reason.isBlank()) {
            log.info("Member withdrawal - ID: {}, Reason: {}", memberId, reason);
        }
    }
}

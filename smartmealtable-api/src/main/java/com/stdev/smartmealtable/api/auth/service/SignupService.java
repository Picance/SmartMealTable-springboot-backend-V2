package com.stdev.smartmealtable.api.auth.service;

import com.stdev.smartmealtable.api.auth.service.dto.CheckEmailServiceResponse;
import com.stdev.smartmealtable.api.auth.service.dto.SignupServiceRequest;
import com.stdev.smartmealtable.api.auth.service.dto.SignupServiceResponse;
import com.stdev.smartmealtable.domain.member.entity.Member;
import com.stdev.smartmealtable.domain.member.entity.MemberAuthentication;
import com.stdev.smartmealtable.domain.member.repository.MemberRepository;
import com.stdev.smartmealtable.domain.member.service.MemberDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회원가입 Application Service
 * 유즈케이스 orchestration에 집중
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SignupService {
    
    private final MemberRepository memberRepository;
    private final MemberDomainService memberDomainService;
    
    /**
     * 이메일 회원가입
     */
    @Transactional
    public SignupServiceResponse signup(SignupServiceRequest request) {
        // 1. Domain Service를 통한 회원 및 인증 정보 생성
        MemberAuthentication savedAuthentication = memberDomainService.createMemberWithAuthentication(
                request.getName(),
                request.getEmail(),
                request.getPassword()
        );
        
        // 2. 회원 정보 조회
        Member savedMember = memberRepository.findById(savedAuthentication.getMemberId())
                .orElseThrow(); // Domain Service에서 이미 생성했으므로 예외 발생하지 않음
        
        // 3. 응답 DTO 생성
        return SignupServiceResponse.of(savedMember, savedAuthentication);
    }

    /**
     * 이메일 중복 검증
     */
    public CheckEmailServiceResponse checkEmail(String email) {
        boolean isDuplicated = memberDomainService.isEmailDuplicated(email);
        return isDuplicated ? CheckEmailServiceResponse.ofDuplicate() : CheckEmailServiceResponse.ofAvailable();
    }
}

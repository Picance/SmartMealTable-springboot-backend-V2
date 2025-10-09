package com.stdev.smartmealtable.api.auth.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.stdev.smartmealtable.api.auth.service.dto.SignupServiceRequest;
import com.stdev.smartmealtable.api.auth.service.dto.SignupServiceResponse;
import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.domain.member.entity.Member;
import com.stdev.smartmealtable.domain.member.entity.MemberAuthentication;
import com.stdev.smartmealtable.domain.member.entity.RecommendationType;
import com.stdev.smartmealtable.domain.member.repository.MemberAuthenticationRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회원가입 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SignupService {
    
    private final MemberRepository memberRepository;
    private final MemberAuthenticationRepository memberAuthenticationRepository;
    
    /**
     * 이메일 회원가입
     */
    @Transactional
    public SignupServiceResponse signup(SignupServiceRequest request) {
        // 1. 이메일 중복 검증
        if (memberAuthenticationRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorType.DUPLICATE_EMAIL);
        }
        
        // 2. Member 생성 (초기값: nickname = name, recommendationType = BALANCED)
        Member member = Member.create(
                null,  // groupId는 null (온보딩 미완료)
                request.getName(),  // 초기 nickname은 name과 동일
                RecommendationType.BALANCED  // 기본 추천 유형
        );
        
        // 3. Member 저장
        Member savedMember = memberRepository.save(member);
        
        // 4. 비밀번호 암호화
        String hashedPassword = BCrypt.withDefaults().hashToString(12, request.getPassword().toCharArray());
        
        // 5. MemberAuthentication 생성
        MemberAuthentication authentication = MemberAuthentication.createEmailAuth(
                savedMember.getMemberId(),
                request.getEmail(),
                hashedPassword,
                request.getName()
        );
        
        // 6. MemberAuthentication 저장
        MemberAuthentication savedAuthentication = memberAuthenticationRepository.save(authentication);
        
        // 7. 응답 DTO 생성
        return SignupServiceResponse.of(savedMember, savedAuthentication);
    }
}

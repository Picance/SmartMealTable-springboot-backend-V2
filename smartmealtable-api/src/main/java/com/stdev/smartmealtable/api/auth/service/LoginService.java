package com.stdev.smartmealtable.api.auth.service;

import com.stdev.smartmealtable.api.auth.service.dto.LoginServiceRequest;
import com.stdev.smartmealtable.api.auth.service.dto.LoginServiceResponse;
import com.stdev.smartmealtable.domain.member.entity.Member;
import com.stdev.smartmealtable.domain.member.entity.MemberAuthentication;
import com.stdev.smartmealtable.domain.member.service.AuthenticationDomainService;
import com.stdev.smartmealtable.domain.member.service.MemberDomainService;
import com.stdev.smartmealtable.support.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 로그인 Application Service
 * 유즈케이스 orchestration에 집중
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoginService {
    
    private final AuthenticationDomainService authenticationDomainService;
    private final MemberDomainService memberDomainService;
    private final JwtTokenProvider jwtTokenProvider;
    
    /**
     * 이메일 로그인
     */
    @Transactional
    public LoginServiceResponse login(LoginServiceRequest request) {
        // 1. Domain Service를 통한 인증 검증
        MemberAuthentication authentication = authenticationDomainService.authenticate(
                request.getEmail(),
                request.getPassword()
        );
        
        // 2. 회원 정보 조회
        Member member = memberDomainService.getMemberById(authentication.getMemberId());
        
        // 3. JWT 토큰 생성 (memberId 기반)
        String accessToken = jwtTokenProvider.createToken(member.getMemberId());
        String refreshToken = jwtTokenProvider.createToken(member.getMemberId());
        
        // 4. 응답 DTO 생성
        return LoginServiceResponse.of(member, authentication, accessToken, refreshToken);
    }
}

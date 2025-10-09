package com.stdev.smartmealtable.api.auth.service;

import com.stdev.smartmealtable.api.auth.service.dto.LoginServiceRequest;
import com.stdev.smartmealtable.api.auth.service.dto.LoginServiceResponse;
import com.stdev.smartmealtable.api.config.JwtConfig;

import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.AuthenticationException;
import com.stdev.smartmealtable.domain.member.entity.Member;
import com.stdev.smartmealtable.domain.member.entity.MemberAuthentication;
import com.stdev.smartmealtable.domain.member.repository.MemberAuthenticationRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 로그인 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoginService {
    
    private final MemberRepository memberRepository;
    private final MemberAuthenticationRepository memberAuthenticationRepository;
    private final JwtConfig.JwtTokenProvider jwtTokenProvider;
    
    /**
     * 이메일 로그인
     */
    @Transactional
    public LoginServiceResponse login(LoginServiceRequest request) {
        // 1. 이메일로 MemberAuthentication 조회
        Optional<MemberAuthentication> authenticationOpt = 
                memberAuthenticationRepository.findByEmail(request.getEmail());
        
        if (authenticationOpt.isEmpty()) {
            throw new AuthenticationException(ErrorType.INVALID_CREDENTIALS);
        }
        
        MemberAuthentication authentication = authenticationOpt.get();
        
        // 2. 비밀번호 검증
        if (!JwtConfig.PasswordEncoder.matches(request.getPassword(), authentication.getPassword())) {
            throw new AuthenticationException(ErrorType.INVALID_CREDENTIALS);
        }
        
        // 3. 회원 정보 조회
        Optional<Member> memberOpt = memberRepository.findById(authentication.getMemberId());
        if (memberOpt.isEmpty()) {
            throw new AuthenticationException(ErrorType.INVALID_CREDENTIALS);
        }
        
        Member member = memberOpt.get();
        
        // 4. JWT 토큰 생성 
        String accessToken = jwtTokenProvider.generateAccessToken(authentication.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication.getEmail());        // 5. 응답 DTO 생성
        return LoginServiceResponse.of(member, authentication, accessToken, refreshToken);
    }
}
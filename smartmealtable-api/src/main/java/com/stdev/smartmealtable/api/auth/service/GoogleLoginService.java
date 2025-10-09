package com.stdev.smartmealtable.api.auth.service;

import com.stdev.smartmealtable.api.auth.dto.request.GoogleLoginServiceRequest;
import com.stdev.smartmealtable.api.auth.dto.response.GoogleLoginServiceResponse;
import com.stdev.smartmealtable.client.auth.oauth.dto.OAuthTokenResponse;
import com.stdev.smartmealtable.client.auth.oauth.dto.OAuthUserInfo;
import com.stdev.smartmealtable.client.auth.oauth.google.GoogleAuthClient;
import com.stdev.smartmealtable.domain.member.entity.Member;
import com.stdev.smartmealtable.domain.member.entity.MemberAuthentication;
import com.stdev.smartmealtable.domain.member.entity.SocialAccount;
import com.stdev.smartmealtable.domain.member.entity.SocialProvider;
import com.stdev.smartmealtable.domain.member.repository.MemberAuthenticationRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberRepository;
import com.stdev.smartmealtable.domain.member.repository.SocialAccountRepository;
import com.stdev.smartmealtable.domain.member.service.SocialAuthDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 구글 로그인 Application Service
 * 구글 OAuth 로그인 유즈케이스를 처리합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleLoginService {

    private final GoogleAuthClient googleAuthClient;
    private final SocialAuthDomainService socialAuthDomainService;
    private final MemberRepository memberRepository;
    private final MemberAuthenticationRepository memberAuthenticationRepository;
    private final SocialAccountRepository socialAccountRepository;

    /**
     * 구글 로그인 처리
     * 
     * @param request 구글 로그인 요청 (인가 코드, 리다이렉트 URI)
     * @return 로그인 결과 (회원 정보, 신규 회원 여부)
     */
    @Transactional
    public GoogleLoginServiceResponse login(GoogleLoginServiceRequest request) {
        log.info("구글 로그인 시작: redirectUri={}", request.redirectUri());

        // 1. 구글 Access Token 발급
        OAuthTokenResponse tokenResponse = googleAuthClient.getAccessToken(
                request.authorizationCode()
        );
        log.debug("구글 Access Token 발급 완료: tokenType={}", tokenResponse.getTokenType());

        // 2. 구글 사용자 정보 조회 (ID Token에서 추출)
        OAuthUserInfo userInfo = googleAuthClient.extractUserInfo(tokenResponse.getIdToken());
        log.info("구글 사용자 정보 조회 완료: providerId={}, email={}", userInfo.getProviderId(), userInfo.getEmail());

        // 3. 기존 소셜 계정 확인
        Optional<SocialAccount> existingSocialAccount = socialAccountRepository.findByProviderAndProviderId(
                SocialProvider.GOOGLE,
                userInfo.getProviderId()
        );

        if (existingSocialAccount.isPresent()) {
            // 기존 회원 로그인
            return handleExistingMember(existingSocialAccount.get(), tokenResponse, userInfo);
        } else {
            // 신규 회원 가입
            return handleNewMember(tokenResponse, userInfo);
        }
    }

    /**
     * 기존 회원 로그인 처리
     */
    private GoogleLoginServiceResponse handleExistingMember(
            SocialAccount socialAccount,
            OAuthTokenResponse tokenResponse,
            OAuthUserInfo userInfo
    ) {
        log.info("기존 회원 로그인 처리: memberAuthenticationId={}", socialAccount.getMemberAuthenticationId());

        // 1. 토큰 업데이트
        socialAuthDomainService.updateSocialAccountToken(
                SocialProvider.GOOGLE,
                userInfo.getProviderId(),
                tokenResponse.getAccessToken(),
                tokenResponse.getRefreshToken(),
                tokenResponse.calculateExpiresAt()
        );

        // 2. 회원 인증 정보 조회
        MemberAuthentication memberAuth = memberAuthenticationRepository.findById(socialAccount.getMemberAuthenticationId())
                .orElseThrow(() -> new IllegalStateException("회원 인증 정보를 찾을 수 없습니다."));

        // 3. 회원 정보 조회
        Member member = memberRepository.findById(memberAuth.getMemberId())
                .orElseThrow(() -> new IllegalStateException("회원 정보를 찾을 수 없습니다."));

        return GoogleLoginServiceResponse.ofExistingMember(
                member.getMemberId(),
                memberAuth.getEmail(),
                memberAuth.getName()
        );
    }

    /**
     * 신규 회원 가입 처리
     */
    private GoogleLoginServiceResponse handleNewMember(
            OAuthTokenResponse tokenResponse,
            OAuthUserInfo userInfo
    ) {
        log.info("신규 회원 가입 처리: providerId={}, email={}", userInfo.getProviderId(), userInfo.getEmail());

        // 1. 회원 생성 및 소셜 계정 연동
        Member member = socialAuthDomainService.createMemberWithSocialAccount(
                userInfo.getEmail(),
                userInfo.getName(),
                SocialProvider.GOOGLE,
                userInfo.getProviderId(),
                tokenResponse.getAccessToken(),
                tokenResponse.getRefreshToken(),
                tokenResponse.getTokenType(),
                tokenResponse.calculateExpiresAt()
        );

        return GoogleLoginServiceResponse.ofNewMember(
                member.getMemberId(),
                userInfo.getEmail(),
                userInfo.getName()
        );
    }
}

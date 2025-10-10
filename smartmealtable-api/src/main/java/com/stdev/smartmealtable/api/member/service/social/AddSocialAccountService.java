package com.stdev.smartmealtable.api.member.service.social;

import com.stdev.smartmealtable.client.auth.oauth.dto.OAuthTokenResponse;
import com.stdev.smartmealtable.client.auth.oauth.dto.OAuthUserInfo;
import com.stdev.smartmealtable.client.auth.oauth.google.GoogleAuthClient;
import com.stdev.smartmealtable.client.auth.oauth.kakao.KakaoAuthClient;
import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.domain.member.entity.MemberAuthentication;
import com.stdev.smartmealtable.domain.member.entity.SocialAccount;
import com.stdev.smartmealtable.domain.member.entity.SocialProvider;
import com.stdev.smartmealtable.domain.member.repository.MemberAuthenticationRepository;
import com.stdev.smartmealtable.domain.member.repository.SocialAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 소셜 계정 추가 연동 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AddSocialAccountService {

    private final SocialAccountRepository socialAccountRepository;
    private final MemberAuthenticationRepository memberAuthenticationRepository;
    private final KakaoAuthClient kakaoAuthClient;
    private final GoogleAuthClient googleAuthClient;

    /**
     * 소셜 계정 추가 연동
     * 
     * @param memberId 회원 ID
     * @param request 소셜 계정 추가 연동 요청
     * @return 소셜 계정 추가 연동 응답
     */
    public AddSocialAccountServiceResponse addSocialAccount(Long memberId, AddSocialAccountServiceRequest request) {
        // 1. MemberAuthentication 조회
        MemberAuthentication memberAuth = memberAuthenticationRepository.findByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorType.MEMBER_NOT_FOUND));

        // 2. OAuth Provider에 따라 토큰 및 사용자 정보 조회
        OAuthTokenResponse tokenResponse;
        OAuthUserInfo userInfo;

        if (request.provider() == SocialProvider.KAKAO) {
            tokenResponse = kakaoAuthClient.getAccessToken(request.authorizationCode());
            userInfo = kakaoAuthClient.extractUserInfo(tokenResponse.getIdToken());
        } else if (request.provider() == SocialProvider.GOOGLE) {
            tokenResponse = googleAuthClient.getAccessToken(request.authorizationCode());
            userInfo = googleAuthClient.extractUserInfo(tokenResponse.getIdToken());
        } else {
            throw new IllegalArgumentException("지원하지 않는 소셜 제공자입니다: " + request.provider());
        }

        // 3. 중복 검증: 이미 다른 계정에 연동된 소셜 계정인지 확인
        boolean alreadyLinked = socialAccountRepository.existsByProviderAndProviderId(
                request.provider(),
                userInfo.getProviderId()
        );

        if (alreadyLinked) {
            log.warn("이미 다른 계정에 연동된 소셜 계정입니다. provider={}, providerId={}", 
                    request.provider(), userInfo.getProviderId());
            throw new BusinessException(ErrorType.SOCIAL_ACCOUNT_ALREADY_LINKED);
        }

        // 4. 소셜 계정 생성 및 저장
        SocialAccount socialAccount = SocialAccount.create(
                memberAuth.getMemberAuthenticationId(),
                request.provider(),
                userInfo.getProviderId(),
                tokenResponse.getAccessToken(),
                tokenResponse.getRefreshToken(),
                tokenResponse.getTokenType(),
                LocalDateTime.now().plusSeconds(tokenResponse.getExpiresIn())
        );

        socialAccount = socialAccountRepository.save(socialAccount);

        log.info("소셜 계정 추가 연동 성공. memberId={}, provider={}, socialAccountId={}", 
                memberId, request.provider(), socialAccount.getSocialAccountId());

        return AddSocialAccountServiceResponse.of(socialAccount, userInfo.getEmail());
    }
}

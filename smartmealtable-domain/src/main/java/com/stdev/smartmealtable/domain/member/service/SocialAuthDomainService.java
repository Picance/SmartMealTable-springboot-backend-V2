package com.stdev.smartmealtable.domain.member.service;

import com.stdev.smartmealtable.domain.member.entity.*;
import com.stdev.smartmealtable.domain.member.repository.MemberAuthenticationRepository;
import com.stdev.smartmealtable.domain.member.repository.MemberRepository;
import com.stdev.smartmealtable.domain.member.repository.SocialAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 소셜 로그인 관련 도메인 서비스
 * 소셜 계정 생성, 조회, 회원 연동 등의 비즈니스 로직을 담당합니다.
 */
@Service
@RequiredArgsConstructor
public class SocialAuthDomainService {

    private final SocialAccountRepository socialAccountRepository;
    private final MemberRepository memberRepository;
    private final MemberAuthenticationRepository memberAuthenticationRepository;

    /**
     * 소셜 계정으로 기존 회원 조회
     * Provider와 Provider ID로 소셜 계정을 찾아, 연결된 회원 정보를 반환합니다.
     *
     * @param provider 소셜 제공자 (KAKAO, GOOGLE)
     * @param providerId 제공자의 사용자 고유 ID
     * @return 회원 정보 (없으면 Empty)
     */
    public Optional<Member> findMemberBySocialAccount(SocialProvider provider, String providerId) {
        return socialAccountRepository.findByProviderAndProviderId(provider, providerId)
                .flatMap(socialAccount -> memberAuthenticationRepository.findById(socialAccount.getMemberAuthenticationId()))
                .flatMap(auth -> memberRepository.findById(auth.getMemberId()));
    }

    /**
     * 소셜 계정 존재 여부 확인
     *
     * @param provider 소셜 제공자
     * @param providerId 제공자의 사용자 고유 ID
     * @return 존재 여부
     */
    public boolean existsSocialAccount(SocialProvider provider, String providerId) {
        return socialAccountRepository.existsByProviderAndProviderId(provider, providerId);
    }

    /**
     * 소셜 계정 생성 및 회원 연동
     * 새로운 회원을 생성하고 소셜 계정과 연동합니다.
     *
     * @param email 이메일
     * @param name 이름
     * @param provider 소셜 제공자
     * @param providerId 제공자의 사용자 고유 ID
     * @param accessToken Access Token
     * @param refreshToken Refresh Token (선택)
     * @param tokenType Token Type
     * @param expiresAt 토큰 만료 시간
     * @return 생성된 회원 정보
     */
    public Member createMemberWithSocialAccount(
            String email,
            String name,
            SocialProvider provider,
            String providerId,
            String accessToken,
            String refreshToken,
            String tokenType,
            LocalDateTime expiresAt
    ) {
    // 1. 회원 생성 (기본 추천 유형: BALANCED)
    Member member = Member.create(null, name, RecommendationType.BALANCED);
    member = memberRepository.save(member);

    // 2. 회원 인증 정보 생성 (소셜 로그인은 비밀번호 없음)
    MemberAuthentication auth = MemberAuthentication.createSocialAuth(member.getMemberId(), email, name);
    auth = memberAuthenticationRepository.save(auth);

    // 3. 소셜 계정 생성 및 저장
    SocialAccount socialAccount = SocialAccount.create(
        auth.getMemberAuthenticationId(),
        provider,
        providerId,
        accessToken,
        refreshToken,
        tokenType,
        expiresAt
    );
    socialAccountRepository.save(socialAccount);

    return member;
    }

    /**
     * 소셜 계정의 토큰 업데이트
     *
     * @param provider 소셜 제공자
     * @param providerId 제공자의 사용자 고유 ID
     * @param newAccessToken 새 Access Token
     * @param newRefreshToken 새 Refresh Token (선택)
     * @param newExpiresAt 새 만료 시간
     */
    public void updateSocialAccountToken(
            SocialProvider provider,
            String providerId,
            String newAccessToken,
            String newRefreshToken,
            LocalDateTime newExpiresAt
    ) {
        SocialAccount socialAccount = socialAccountRepository.findByProviderAndProviderId(provider, providerId)
                .orElseThrow(() -> new IllegalArgumentException("소셜 계정을 찾을 수 없습니다."));

        socialAccount.updateToken(newAccessToken, newRefreshToken, newExpiresAt);
        socialAccountRepository.save(socialAccount);
    }

    /**
     * 소셜 계정으로 회원 인증 정보 조회
     *
     * @param provider 소셜 제공자
     * @param providerId 제공자의 사용자 고유 ID
     * @return 회원 인증 정보 (없으면 Empty)
     */
    public Optional<MemberAuthentication> findAuthenticationBySocialAccount(SocialProvider provider, String providerId) {
        return socialAccountRepository.findByProviderAndProviderId(provider, providerId)
                .flatMap(socialAccount -> memberAuthenticationRepository.findById(socialAccount.getMemberAuthenticationId()));
    }
}

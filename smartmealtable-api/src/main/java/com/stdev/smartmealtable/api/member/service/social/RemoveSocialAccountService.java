package com.stdev.smartmealtable.api.member.service.social;

import com.stdev.smartmealtable.core.error.ErrorType;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.domain.member.entity.MemberAuthentication;
import com.stdev.smartmealtable.domain.member.entity.SocialAccount;
import com.stdev.smartmealtable.domain.member.repository.MemberAuthenticationRepository;
import com.stdev.smartmealtable.domain.member.repository.SocialAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 소셜 계정 연동 해제 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RemoveSocialAccountService {

    private final SocialAccountRepository socialAccountRepository;
    private final MemberAuthenticationRepository memberAuthenticationRepository;

    /**
     * 소셜 계정 연동 해제
     * 
     * @param memberId 회원 ID
     * @param socialAccountId 소셜 계정 ID
     */
    public void removeSocialAccount(Long memberId, Long socialAccountId) {
        // 1. MemberAuthentication 조회
        MemberAuthentication memberAuth = memberAuthenticationRepository.findByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorType.MEMBER_NOT_FOUND));

        // 2. SocialAccount 조회
        SocialAccount socialAccount = socialAccountRepository.findById(socialAccountId)
                .orElseThrow(() -> new BusinessException(ErrorType.SOCIAL_ACCOUNT_NOT_FOUND));

        // 3. 본인의 소셜 계정인지 확인
        if (!socialAccount.getMemberAuthenticationId().equals(memberAuth.getMemberAuthenticationId())) {
            log.warn("다른 사용자의 소셜 계정입니다. memberId={}, socialAccountId={}", 
                    memberId, socialAccountId);
            throw new BusinessException(ErrorType.ACCESS_DENIED);
        }

        // 4. 유일한 로그인 수단인지 확인
        boolean hasPassword = memberAuth.hasPassword();
        List<SocialAccount> allSocialAccounts = socialAccountRepository
                .findByMemberAuthenticationId(memberAuth.getMemberAuthenticationId());

        if (!hasPassword && allSocialAccounts.size() == 1) {
            log.warn("유일한 로그인 수단입니다. memberId={}, socialAccountId={}", 
                    memberId, socialAccountId);
            throw new BusinessException(ErrorType.LAST_LOGIN_METHOD);
        }

        // 5. 소셜 계정 삭제
        socialAccountRepository.delete(socialAccount);

        log.info("소셜 계정 연동 해제 성공. memberId={}, socialAccountId={}", 
                memberId, socialAccountId);
    }
}

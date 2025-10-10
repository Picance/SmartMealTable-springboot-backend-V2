package com.stdev.smartmealtable.api.member.service.social;

import com.stdev.smartmealtable.domain.member.entity.MemberAuthentication;
import com.stdev.smartmealtable.domain.member.entity.SocialAccount;
import com.stdev.smartmealtable.domain.member.repository.MemberAuthenticationRepository;
import com.stdev.smartmealtable.domain.member.repository.SocialAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 소셜 계정 목록 조회 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetSocialAccountListService {

    private final SocialAccountRepository socialAccountRepository;
    private final MemberAuthenticationRepository memberAuthenticationRepository;

    /**
     * 연동된 소셜 계정 목록 및 비밀번호 설정 여부 조회
     * 
     * @param memberId 회원 ID
     * @return 소셜 계정 목록 및 비밀번호 설정 여부
     */
    public SocialAccountListServiceResponse getSocialAccountList(Long memberId) {
        // 1. MemberAuthentication 조회 (이메일 및 비밀번호 확인용)
        MemberAuthentication memberAuth = memberAuthenticationRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalStateException("회원 인증 정보를 찾을 수 없습니다."));

        // 2. 소셜 계정 목록 조회
        List<SocialAccount> socialAccounts = socialAccountRepository
                .findByMemberAuthenticationId(memberAuth.getMemberAuthenticationId());

        // 3. 각 소셜 계정에 대해 이메일 정보 매핑
        // 참고: 현재 아키텍처에서는 모든 소셜 계정이 동일한 MemberAuthentication을 공유하므로
        // 이메일은 MemberAuthentication.email을 사용합니다.
        List<ConnectedSocialAccountResponse> connectedAccounts = socialAccounts.stream()
                .map(account -> ConnectedSocialAccountResponse.from(account, memberAuth.getEmail()))
                .toList();

        // 4. 비밀번호 설정 여부 확인
        Boolean hasPassword = memberAuth.hasPassword();

        return SocialAccountListServiceResponse.of(connectedAccounts, hasPassword);
    }
}

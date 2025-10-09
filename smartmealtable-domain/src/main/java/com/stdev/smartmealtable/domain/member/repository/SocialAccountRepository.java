package com.stdev.smartmealtable.domain.member.repository;

import com.stdev.smartmealtable.domain.member.entity.SocialAccount;
import com.stdev.smartmealtable.domain.member.entity.SocialProvider;

import java.util.List;
import java.util.Optional;

/**
 * SocialAccount Repository 인터페이스
 * Storage 계층에서 구현
 */
public interface SocialAccountRepository {

    /**
     * 소셜 계정 저장
     */
    SocialAccount save(SocialAccount socialAccount);

    /**
     * 소셜 계정 조회 by ID
     */
    Optional<SocialAccount> findById(Long socialAccountId);

    /**
     * 소셜 계정 조회 by Member Authentication ID
     */
    List<SocialAccount> findByMemberAuthenticationId(Long memberAuthenticationId);

    /**
     * 소셜 계정 조회 by Provider and Provider ID
     */
    Optional<SocialAccount> findByProviderAndProviderId(SocialProvider provider, String providerId);

    /**
     * 소셜 계정 존재 여부 확인
     */
    boolean existsByProviderAndProviderId(SocialProvider provider, String providerId);

    /**
     * 소셜 계정 삭제
     */
    void delete(SocialAccount socialAccount);
}

package com.stdev.smartmealtable.storage.db.member.repository;

import com.stdev.smartmealtable.domain.member.entity.SocialProvider;
import com.stdev.smartmealtable.storage.db.member.entity.SocialAccountJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * SocialAccount Spring Data JPA Repository
 */
public interface SocialAccountJpaRepository extends JpaRepository<SocialAccountJpaEntity, Long> {

    /**
     * Member Authentication ID로 소셜 계정 목록 조회
     */
    List<SocialAccountJpaEntity> findByMemberAuthenticationId(Long memberAuthenticationId);

    /**
     * Provider와 Provider ID로 소셜 계정 조회
     */
    Optional<SocialAccountJpaEntity> findByProviderAndProviderId(SocialProvider provider, String providerId);

    /**
     * Provider와 Provider ID로 존재 여부 확인
     */
    boolean existsByProviderAndProviderId(SocialProvider provider, String providerId);
}

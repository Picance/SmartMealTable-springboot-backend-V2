package com.stdev.smartmealtable.storage.db.member.repository;

import com.stdev.smartmealtable.domain.member.entity.SocialProvider;
import com.stdev.smartmealtable.storage.db.member.entity.SocialAccountJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * SocialAccount Spring Data JPA Repository
 */
public interface SocialAccountJpaRepository extends JpaRepository<SocialAccountJpaEntity, Long> {

    /**
     * Member Authentication ID로 소셜 계정 목록 조회
     */
    @Query("SELECT s FROM SocialAccountJpaEntity s WHERE s.memberAuthenticationId = :memberAuthenticationId")
    List<SocialAccountJpaEntity> findByMemberAuthenticationId(@Param("memberAuthenticationId") Long memberAuthenticationId);

    /**
     * Provider와 Provider ID로 소셜 계정 조회
     */
    @Query("SELECT s FROM SocialAccountJpaEntity s WHERE s.provider = :provider AND s.providerId = :providerId")
    Optional<SocialAccountJpaEntity> findByProviderAndProviderId(
            @Param("provider") SocialProvider provider,
            @Param("providerId") String providerId
    );

    /**
     * Provider와 Provider ID로 존재 여부 확인
     */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN TRUE ELSE FALSE END FROM SocialAccountJpaEntity s WHERE s.provider = :provider AND s.providerId = :providerId")
    boolean existsByProviderAndProviderId(
            @Param("provider") SocialProvider provider,
            @Param("providerId") String providerId
    );
}

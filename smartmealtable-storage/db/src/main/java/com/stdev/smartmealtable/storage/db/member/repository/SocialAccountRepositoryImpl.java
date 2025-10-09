package com.stdev.smartmealtable.storage.db.member.repository;

import com.stdev.smartmealtable.domain.member.entity.SocialAccount;
import com.stdev.smartmealtable.domain.member.entity.SocialProvider;
import com.stdev.smartmealtable.domain.member.repository.SocialAccountRepository;
import com.stdev.smartmealtable.storage.db.member.entity.SocialAccountJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * SocialAccount Repository 구현체
 */
@Repository
@RequiredArgsConstructor
public class SocialAccountRepositoryImpl implements SocialAccountRepository {

    private final SocialAccountJpaRepository jpaRepository;

    @Override
    public SocialAccount save(SocialAccount socialAccount) {
        SocialAccountJpaEntity entity;
        
        if (socialAccount.getSocialAccountId() == null) {
            // 신규 저장
            entity = SocialAccountJpaEntity.from(socialAccount);
            entity = jpaRepository.save(entity);
        } else {
            // 기존 엔티티 업데이트
            entity = jpaRepository.findById(socialAccount.getSocialAccountId())
                    .orElseThrow(() -> new IllegalArgumentException("소셜 계정을 찾을 수 없습니다. ID: " + socialAccount.getSocialAccountId()));
            entity.syncFromDomain(socialAccount);
            entity = jpaRepository.save(entity);
        }
        
        return entity.toDomain();
    }

    @Override
    public Optional<SocialAccount> findById(Long socialAccountId) {
        return jpaRepository.findById(socialAccountId)
                .map(SocialAccountJpaEntity::toDomain);
    }

    @Override
    public List<SocialAccount> findByMemberAuthenticationId(Long memberAuthenticationId) {
        return jpaRepository.findByMemberAuthenticationId(memberAuthenticationId).stream()
                .map(SocialAccountJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<SocialAccount> findByProviderAndProviderId(SocialProvider provider, String providerId) {
        return jpaRepository.findByProviderAndProviderId(provider, providerId)
                .map(SocialAccountJpaEntity::toDomain);
    }

    @Override
    public boolean existsByProviderAndProviderId(SocialProvider provider, String providerId) {
        return jpaRepository.existsByProviderAndProviderId(provider, providerId);
    }

    @Override
    public void delete(SocialAccount socialAccount) {
        if (socialAccount.getSocialAccountId() != null) {
            jpaRepository.deleteById(socialAccount.getSocialAccountId());
        }
    }
}

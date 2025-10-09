package com.stdev.smartmealtable.storage.db.member.repository;

import com.stdev.smartmealtable.domain.member.entity.MemberAuthentication;
import com.stdev.smartmealtable.domain.member.repository.MemberAuthenticationRepository;
import com.stdev.smartmealtable.storage.db.member.entity.MemberAuthenticationJpaEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * MemberAuthentication Repository 구현체
 */
@Repository
public class MemberAuthenticationRepositoryImpl implements MemberAuthenticationRepository {

    private final MemberAuthenticationJpaRepository jpaRepository;

    public MemberAuthenticationRepositoryImpl(MemberAuthenticationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public MemberAuthentication save(MemberAuthentication memberAuthentication) {
        MemberAuthenticationJpaEntity jpaEntity = MemberAuthenticationJpaEntity.from(memberAuthentication);
        MemberAuthenticationJpaEntity saved = jpaRepository.save(jpaEntity);
        return saved.toDomain();
    }

    @Override
    public Optional<MemberAuthentication> findById(Long memberAuthenticationId) {
        return jpaRepository.findById(memberAuthenticationId)
                .map(MemberAuthenticationJpaEntity::toDomain);
    }

    @Override
    public Optional<MemberAuthentication> findByMemberId(Long memberId) {
        return jpaRepository.findByMemberId(memberId)
                .map(MemberAuthenticationJpaEntity::toDomain);
    }

    @Override
    public Optional<MemberAuthentication> findByEmail(String email) {
        return jpaRepository.findByEmail(email)
                .map(MemberAuthenticationJpaEntity::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public void delete(MemberAuthentication memberAuthentication) {
        MemberAuthenticationJpaEntity jpaEntity = MemberAuthenticationJpaEntity.from(memberAuthentication);
        jpaRepository.delete(jpaEntity);
    }
}

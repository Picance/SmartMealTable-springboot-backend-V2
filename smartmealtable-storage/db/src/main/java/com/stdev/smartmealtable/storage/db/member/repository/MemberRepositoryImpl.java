package com.stdev.smartmealtable.storage.db.member.repository;

import com.stdev.smartmealtable.domain.member.entity.Member;
import com.stdev.smartmealtable.domain.member.repository.MemberRepository;
import com.stdev.smartmealtable.storage.db.member.entity.MemberJpaEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Member Repository 구현체
 * Domain Repository 인터페이스를 구현하여 JPA와 Domain을 연결
 */
@Repository
public class MemberRepositoryImpl implements MemberRepository {

    private final MemberJpaRepository memberJpaRepository;

    public MemberRepositoryImpl(MemberJpaRepository memberJpaRepository) {
        this.memberJpaRepository = memberJpaRepository;
    }

    @Override
    public Member save(Member member) {
        MemberJpaEntity jpaEntity = MemberJpaEntity.from(member);
        MemberJpaEntity saved = memberJpaRepository.save(jpaEntity);
        return saved.toDomain();
    }

    @Override
    public Optional<Member> findById(Long memberId) {
        return memberJpaRepository.findById(memberId)
                .map(MemberJpaEntity::toDomain);
    }

    @Override
    public boolean existsByNickname(String nickname) {
        return memberJpaRepository.existsByNickname(nickname);
    }

    @Override
    public void delete(Member member) {
        MemberJpaEntity jpaEntity = MemberJpaEntity.from(member);
        memberJpaRepository.delete(jpaEntity);
    }
}

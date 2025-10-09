package com.stdev.smartmealtable.domain.member.repository;

import com.stdev.smartmealtable.domain.member.entity.Member;

import java.util.Optional;

/**
 * Member Repository 인터페이스
 * Storage 계층에서 구현
 */
public interface MemberRepository {

    /**
     * 회원 저장
     */
    Member save(Member member);

    /**
     * 회원 조회 by ID
     */
    Optional<Member> findById(Long memberId);

    /**
     * 닉네임 중복 확인
     */
    boolean existsByNickname(String nickname);

    /**
     * 회원 삭제
     */
    void delete(Member member);
}

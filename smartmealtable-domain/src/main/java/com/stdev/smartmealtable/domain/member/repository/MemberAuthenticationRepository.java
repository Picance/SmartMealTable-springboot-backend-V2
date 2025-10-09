package com.stdev.smartmealtable.domain.member.repository;

import com.stdev.smartmealtable.domain.member.entity.MemberAuthentication;

import java.util.Optional;

/**
 * MemberAuthentication Repository 인터페이스
 * Storage 계층에서 구현
 */
public interface MemberAuthenticationRepository {

    /**
     * 회원 인증 정보 저장
     */
    MemberAuthentication save(MemberAuthentication memberAuthentication);

    /**
     * 회원 인증 정보 조회 by ID
     */
    Optional<MemberAuthentication> findById(Long memberAuthenticationId);

    /**
     * 회원 인증 정보 조회 by Member ID
     */
    Optional<MemberAuthentication> findByMemberId(Long memberId);

    /**
     * 회원 인증 정보 조회 by Email
     */
    Optional<MemberAuthentication> findByEmail(String email);

    /**
     * 이메일 중복 확인
     */
    boolean existsByEmail(String email);

    /**
     * 회원 인증 정보 삭제
     */
    void delete(MemberAuthentication memberAuthentication);
}

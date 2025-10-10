package com.stdev.smartmealtable.domain.member.repository;

import com.stdev.smartmealtable.domain.member.entity.AddressHistory;

import java.util.List;
import java.util.Optional;

/**
 * 주소 이력 Repository 인터페이스
 */
public interface AddressHistoryRepository {
    
    /**
     * 주소 이력 저장
     */
    AddressHistory save(AddressHistory addressHistory);
    
    /**
     * ID로 주소 이력 조회
     */
    Optional<AddressHistory> findById(Long addressHistoryId);
    
    /**
     * 회원의 모든 주소 이력 조회 (삭제되지 않은 것만)
     */
    List<AddressHistory> findAllByMemberId(Long memberId);
    
    /**
     * 회원의 주 주소 조회
     */
    Optional<AddressHistory> findPrimaryByMemberId(Long memberId);
    
    /**
     * 회원의 주소 개수 조회 (삭제되지 않은 것만)
     */
    long countByMemberId(Long memberId);
    
    /**
     * 주소 이력 삭제 (Soft Delete)
     */
    void delete(AddressHistory addressHistory);
    
    /**
     * 회원의 모든 주소를 기본 주소 아님으로 설정
     */
    void unmarkAllAsPrimaryByMemberId(Long memberId);
}

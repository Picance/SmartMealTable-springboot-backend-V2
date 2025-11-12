package com.stdev.smartmealtable.domain.cart;

import java.util.List;
import java.util.Optional;

/**
 * 장바구니 Repository 인터페이스
 */
public interface CartRepository {
    
    /**
     * 장바구니 저장
     */
    Cart save(Cart cart);
    
    /**
     * 장바구니 ID로 조회
     */
    Optional<Cart> findById(Long cartId);
    
    /**
     * 회원 ID와 가게 ID로 장바구니 조회
     */
    Optional<Cart> findByMemberIdAndStoreId(Long memberId, Long storeId);
    
    /**
     * 회원의 모든 장바구니 조회
     */
    List<Cart> findByMemberId(Long memberId);
    
    /**
     * 장바구니 삭제
     */
    void delete(Cart cart);
    
    /**
     * 회원의 특정 가게를 제외한 다른 장바구니 삭제
     */
    void deleteByMemberIdAndStoreIdNot(Long memberId, Long storeId);

    /**
     * 회원의 모든 장바구니 삭제
     */
    void deleteByMemberId(Long memberId);

    /**
     * 장바구니 존재 여부 확인
     */
    boolean existsByMemberIdAndStoreId(Long memberId, Long storeId);
}

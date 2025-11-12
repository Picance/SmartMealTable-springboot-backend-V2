package com.stdev.smartmealtable.domain.cart;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 장바구니 도메인 서비스
 * 복잡한 비즈니스 로직을 처리
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartDomainService {
    
    private final CartRepository cartRepository;
    
    /**
     * 회원과 가게로 장바구니 조회 또는 생성
     * 없으면 새로 생성하여 반환
     * 
     * @param memberId 회원 ID
     * @param storeId 가게 ID
     * @return Cart 엔티티
     */
    @Transactional
    public Cart getOrCreateCart(Long memberId, Long storeId) {
        return cartRepository.findByMemberIdAndStoreId(memberId, storeId)
                .orElseGet(() -> {
                    Cart newCart = Cart.create(memberId, storeId);
                    return cartRepository.save(newCart);
                });
    }
    
    /**
     * 장바구니 전체 삭제 (비우기)
     * 
     * @param memberId 회원 ID
     * @param storeId 가게 ID
     */
    @Transactional
    public void clearCart(Long memberId, Long storeId) {
        cartRepository.findByMemberIdAndStoreId(memberId, storeId)
                .ifPresent(cart -> {
                    cart.clear();
                    if (cart.isEmpty()) {
                        cartRepository.delete(cart);
                    } else {
                        cartRepository.save(cart);
                    }
                });
    }
    
    /**
     * 특정 가게의 장바구니를 제외한 다른 장바구니 삭제
     * (다른 가게의 장바구니를 비우고 새 가게 장바구니 시작)
     *
     * @param memberId 회원 ID
     * @param keepStoreId 유지할 가게 ID
     */
    @Transactional
    public void clearOtherCarts(Long memberId, Long keepStoreId) {
        cartRepository.deleteByMemberIdAndStoreIdNot(memberId, keepStoreId);
    }

    /**
     * 회원의 모든 장바구니 삭제 (전체 비우기)
     *
     * @param memberId 회원 ID
     */
    @Transactional
    public void clearAllCarts(Long memberId) {
        cartRepository.deleteByMemberId(memberId);
    }
}

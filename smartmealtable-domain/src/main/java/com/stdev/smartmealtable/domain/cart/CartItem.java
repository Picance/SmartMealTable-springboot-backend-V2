package com.stdev.smartmealtable.domain.cart;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 장바구니 항목 도메인 엔티티
 * 장바구니에 담긴 개별 음식의 정보를 저장
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CartItem {
    
    /**
     * 장바구니 항목 고유 식별자
     */
    private Long cartItemId;
    
    /**
     * 이 항목이 속한 장바구니의 식별자
     */
    private Long cartId;
    
    /**
     * 장바구니에 담은 음식의 식별자 (외부 참조)
     */
    private Long foodId;
    
    /**
     * 장바구니에 담은 음식의 수량
     */
    private int quantity;
    
    /**
     * 정적 팩토리 메서드 - 새 장바구니 항목 생성
     * 
     * @param cartId 장바구니 ID
     * @param foodId 음식 ID
     * @param quantity 수량
     * @return 생성된 CartItem 엔티티
     */
    public static CartItem create(Long cartId, Long foodId, int quantity) {
        validateQuantity(quantity);
        
        return CartItem.builder()
                .cartId(cartId)
                .foodId(foodId)
                .quantity(quantity)
                .build();
    }
    
    /**
     * 수량 증가
     * 
     * @param additionalQuantity 추가할 수량
     */
    public void increaseQuantity(int additionalQuantity) {
        validateQuantity(additionalQuantity);
        this.quantity += additionalQuantity;
    }
    
    /**
     * 수량 변경
     * 
     * @param newQuantity 새로운 수량
     */
    public void updateQuantity(int newQuantity) {
        validateQuantity(newQuantity);
        this.quantity = newQuantity;
    }
    
    /**
     * 수량 유효성 검증
     */
    private static void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        }
    }
}

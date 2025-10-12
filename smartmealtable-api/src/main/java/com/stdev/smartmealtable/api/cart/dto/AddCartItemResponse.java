package com.stdev.smartmealtable.api.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 장바구니 아이템 추가 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddCartItemResponse {
    
    /**
     * 장바구니 ID
     */
    private Long cartId;
    
    /**
     * 장바구니 아이템 ID
     */
    private Long cartItemId;
    
    /**
     * 음식 ID
     */
    private Long foodId;
    
    /**
     * 수량
     */
    private int quantity;
}

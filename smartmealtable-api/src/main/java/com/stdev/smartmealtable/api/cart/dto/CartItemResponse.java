package com.stdev.smartmealtable.api.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 장바구니 아이템 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    
    /**
     * 장바구니 아이템 ID
     */
    private Long cartItemId;
    
    /**
     * 음식 ID
     */
    private Long foodId;
    
    /**
     * 음식 이름
     */
    private String foodName;
    
    /**
     * 음식 이미지 URL
     */
    private String imageUrl;
    
    /**
     * 평균 가격
     */
    private Integer averagePrice;
    
    /**
     * 수량
     */
    private int quantity;
    
    /**
     * 소계 (가격 × 수량)
     */
    private int subtotal;
}

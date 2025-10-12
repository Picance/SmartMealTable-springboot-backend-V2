package com.stdev.smartmealtable.storage.db.cart;

import com.stdev.smartmealtable.domain.cart.CartItem;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 장바구니 항목 JPA Entity
 * 장바구니에 담긴 개별 음식의 정보를 저장
 */
@Entity
@Table(name = "cart_item",
       uniqueConstraints = {
           @UniqueConstraint(name = "uq_cart_food", columnNames = {"cart_id", "food_id"})
       })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItemEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_item_id")
    private Long cartItemId;
    
    /**
     * 이 항목이 속한 장바구니 (다대일 관계)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private CartEntity cart;
    
    @Column(name = "food_id", nullable = false)
    private Long foodId;
    
    @Column(name = "quantity", nullable = false)
    private int quantity;
    
    // created_at, updated_at은 DB DEFAULT CURRENT_TIMESTAMP로 관리 (도메인에 노출 안 함)
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
    
    /**
     * 양방향 관계 설정을 위한 메서드
     */
    protected void setCart(CartEntity cart) {
        this.cart = cart;
    }
    
    /**
     * 수량 변경
     */
    public void updateQuantity(int newQuantity) {
        this.quantity = newQuantity;
    }
    
    /**
     * Domain Entity → JPA Entity 변환 (신규 생성)
     */
    public static CartItemEntity fromDomain(CartItem item) {
        CartItemEntity entity = new CartItemEntity();
        entity.foodId = item.getFoodId();
        entity.quantity = item.getQuantity();
        return entity;
    }
    
    /**
     * Domain Entity → JPA Entity 변환 (ID 보존)
     */
    public static CartItemEntity fromDomainWithId(CartItem item) {
        CartItemEntity entity = new CartItemEntity();
        entity.cartItemId = item.getCartItemId();
        entity.foodId = item.getFoodId();
        entity.quantity = item.getQuantity();
        return entity;
    }
    
    /**
     * JPA Entity → Domain Entity 변환
     */
    public CartItem toDomain() {
        return CartItem.builder()
                .cartItemId(this.cartItemId)
                .cartId(this.cart != null ? this.cart.getCartId() : null)
                .foodId(this.foodId)
                .quantity(this.quantity)
                .build();
    }
}

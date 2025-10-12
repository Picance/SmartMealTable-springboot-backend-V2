package com.stdev.smartmealtable.storage.db.cart;

import com.stdev.smartmealtable.domain.cart.Cart;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 장바구니 JPA Entity
 * 회원이 선택한 음식들을 임시로 담아두는 공간
 */
@Entity
@Table(name = "cart",
       indexes = {
           @Index(name = "idx_member_id", columnList = "member_id"),
           @Index(name = "idx_store_id", columnList = "store_id")
       })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Long cartId;
    
    @Column(name = "member_id", nullable = false)
    private Long memberId;
    
    @Column(name = "store_id", nullable = false)
    private Long storeId;
    
    /**
     * 장바구니 항목들 (Aggregate Root)
     * CascadeType.ALL: Cart 저장/삭제 시 자동으로 CartItem도 처리
     * orphanRemoval: Cart에서 제거된 CartItem은 자동 삭제
     */
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CartItemEntity> items = new ArrayList<>();
    
    // created_at, updated_at은 DB DEFAULT CURRENT_TIMESTAMP로 관리 (도메인에 노출 안 함)
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
    
    /**
     * Domain Entity → JPA Entity 변환 (신규 생성)
     */
    public static CartEntity fromDomain(Cart cart) {
        CartEntity entity = new CartEntity();
        entity.memberId = cart.getMemberId();
        entity.storeId = cart.getStoreId();
        
        // CartItem 변환 및 양방향 관계 설정
        if (cart.getItems() != null && !cart.getItems().isEmpty()) {
            List<CartItemEntity> itemEntities = cart.getItems().stream()
                    .map(CartItemEntity::fromDomain)
                    .collect(Collectors.toList());
            
            itemEntities.forEach(itemEntity -> itemEntity.setCart(entity));
            entity.items = itemEntities;
        }
        
        return entity;
    }
    
    /**
     * Domain Entity → JPA Entity 변환 (ID 보존)
     */
    public static CartEntity fromDomainWithId(Cart cart) {
        CartEntity entity = new CartEntity();
        entity.cartId = cart.getCartId();
        entity.memberId = cart.getMemberId();
        entity.storeId = cart.getStoreId();
        
        // CartItem 변환 및 양방향 관계 설정
        if (cart.getItems() != null && !cart.getItems().isEmpty()) {
            List<CartItemEntity> itemEntities = cart.getItems().stream()
                    .map(item -> {
                        CartItemEntity itemEntity;
                        if (item.getCartItemId() != null) {
                            itemEntity = CartItemEntity.fromDomainWithId(item);
                        } else {
                            itemEntity = CartItemEntity.fromDomain(item);
                        }
                        return itemEntity;
                    })
                    .collect(Collectors.toList());
            
            itemEntities.forEach(itemEntity -> itemEntity.setCart(entity));
            entity.items = itemEntities;
        }
        
        return entity;
    }
    
    /**
     * JPA Entity → Domain Entity 변환
     */
    public Cart toDomain() {
        Cart cart = Cart.builder()
                .cartId(this.cartId)
                .memberId(this.memberId)
                .storeId(this.storeId)
                .items(this.items.stream()
                        .map(CartItemEntity::toDomain)
                        .collect(Collectors.toList()))
                .build();
        
        return cart;
    }
    
    /**
     * CartItem 추가 (양방향 관계 설정)
     */
    public void addItem(CartItemEntity item) {
        items.add(item);
        item.setCart(this);
    }
    
    /**
     * CartItem 제거 (양방향 관계 해제)
     */
    public void removeItem(CartItemEntity item) {
        items.remove(item);
        item.setCart(null);
    }
}

package com.stdev.smartmealtable.storage.db.cart;

import com.stdev.smartmealtable.domain.cart.Cart;

/**
 * CartEntity <-> Cart 도메인 엔티티 매퍼
 */
public class CartMapper {
    
    /**
     * 도메인 엔티티를 JPA 엔티티로 변환
     * 
     * @param cart 도메인 엔티티
     * @return JPA 엔티티
     */
    public static CartEntity toEntity(Cart cart) {
        if (cart.getCartId() == null) {
            return CartEntity.fromDomain(cart);
        } else {
            return CartEntity.fromDomainWithId(cart);
        }
    }
    
    /**
     * JPA 엔티티를 도메인 엔티티로 변환
     * 
     * @param entity JPA 엔티티
     * @return 도메인 엔티티
     */
    public static Cart toDomain(CartEntity entity) {
        return entity.toDomain();
    }
}

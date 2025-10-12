package com.stdev.smartmealtable.storage.db.cart;

import com.stdev.smartmealtable.domain.cart.Cart;
import com.stdev.smartmealtable.domain.cart.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * CartRepository 구현체
 */
@Repository
@RequiredArgsConstructor
public class CartRepositoryImpl implements CartRepository {
    
    private final CartJpaRepository cartJpaRepository;
    
    @Override
    public Cart save(Cart cart) {
        CartEntity entity = CartMapper.toEntity(cart);
        CartEntity savedEntity = cartJpaRepository.save(entity);
        return CartMapper.toDomain(savedEntity);
    }
    
    @Override
    public Optional<Cart> findById(Long cartId) {
        return cartJpaRepository.findById(cartId)
                .map(CartMapper::toDomain);
    }
    
    @Override
    public Optional<Cart> findByMemberIdAndStoreId(Long memberId, Long storeId) {
        return cartJpaRepository.findByMemberIdAndStoreIdWithItems(memberId, storeId)
                .map(CartMapper::toDomain);
    }
    
    @Override
    public List<Cart> findByMemberId(Long memberId) {
        return cartJpaRepository.findByMemberIdWithItems(memberId)
                .stream()
                .map(CartMapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public void delete(Cart cart) {
        if (cart.getCartId() != null) {
            cartJpaRepository.deleteById(cart.getCartId());
        }
    }
    
    @Override
    public void deleteByMemberIdAndStoreIdNot(Long memberId, Long storeId) {
        cartJpaRepository.deleteByMemberIdAndStoreIdNot(memberId, storeId);
    }
    
    @Override
    public boolean existsByMemberIdAndStoreId(Long memberId, Long storeId) {
        return cartJpaRepository.existsByMemberIdAndStoreId(memberId, storeId);
    }
}

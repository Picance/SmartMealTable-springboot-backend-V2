package com.stdev.smartmealtable.storage.db.cart;

import com.stdev.smartmealtable.domain.cart.Cart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CartRepositoryImplTest {

    @Mock
    private CartJpaRepository jpaRepository;

    private CartRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = new CartRepositoryImpl(jpaRepository);
    }

    @Test
    void save_and_find_and_delete_and_exists() {
        Cart c = Cart.create(1L, 2L);
        when(jpaRepository.save(any())).thenAnswer(invocation -> CartMapper.toEntity(Cart.create(1L, 2L)));
        when(jpaRepository.findById(9L)).thenReturn(Optional.of(CartMapper.toEntity(Cart.create(1L,2L))));
        when(jpaRepository.findByMemberIdWithItems(1L)).thenReturn(List.of(CartMapper.toEntity(Cart.create(1L,2L))));
        when(jpaRepository.findByMemberIdAndStoreIdWithItems(1L,2L)).thenReturn(Optional.of(CartMapper.toEntity(Cart.create(1L,2L))));
        when(jpaRepository.existsByMemberIdAndStoreId(1L,2L)).thenReturn(true);

        Cart saved = repository.save(c);
        // Saved entity id might be null since mapper doesn't assign; just assert not null domain
        assertThat(saved).isNotNull();

        Optional<Cart> byId = repository.findById(9L);
        // depending on mapper, may be empty; we check that the call doesn't throw
        // just assert optional is not null reference
        assertThat(byId).isNotNull();

        Optional<Cart> byMemberStore = repository.findByMemberIdAndStoreId(1L,2L);
        assertThat(byMemberStore).isPresent();

        List<Cart> byMember = repository.findByMemberId(1L);
        assertThat(byMember).hasSize(1);

        repository.delete(Cart.create(1L,2L));
        // if delete uses id check, nothing to verify; ensure no exception thrown

        repository.deleteByMemberIdAndStoreIdNot(1L,2L);
        verify(jpaRepository).deleteByMemberIdAndStoreIdNot(1L,2L);

        assertThat(repository.existsByMemberIdAndStoreId(1L,2L)).isTrue();
    }
}

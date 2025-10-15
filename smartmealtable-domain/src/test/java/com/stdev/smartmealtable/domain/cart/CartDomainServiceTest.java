package com.stdev.smartmealtable.domain.cart;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

/**
 * CartDomainService 단위 테스트 (BDD Mockist 스타일)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CartDomainService 테스트")
class CartDomainServiceTest {

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private CartDomainService cartDomainService;

    @Nested
    @DisplayName("getOrCreateCart 메서드는")
    class Describe_getOrCreateCart {

        @Nested
        @DisplayName("장바구니가 이미 존재하면")
        class Context_with_existing_cart {

            @Test
            @DisplayName("기존 장바구니를 반환한다")
            void it_returns_existing_cart() {
                // Given
                Long memberId = 1L;
                Long storeId = 100L;

                Cart existingCart = Cart.create(memberId, storeId);

                given(cartRepository.findByMemberIdAndStoreId(memberId, storeId))
                        .willReturn(Optional.of(existingCart));

                // When
                Cart result = cartDomainService.getOrCreateCart(memberId, storeId);

                // Then
                assertThat(result).isNotNull();
                assertThat(result).isEqualTo(existingCart);
                then(cartRepository).should(times(1)).findByMemberIdAndStoreId(memberId, storeId);
                then(cartRepository).should(times(0)).save(any(Cart.class));
            }
        }

        @Nested
        @DisplayName("장바구니가 존재하지 않으면")
        class Context_with_no_existing_cart {

            @Test
            @DisplayName("새 장바구니를 생성하고 저장한다")
            void it_creates_and_saves_new_cart() {
                // Given
                Long memberId = 1L;
                Long storeId = 100L;

                Cart newCart = Cart.create(memberId, storeId);

                given(cartRepository.findByMemberIdAndStoreId(memberId, storeId))
                        .willReturn(Optional.empty());
                given(cartRepository.save(any(Cart.class))).willReturn(newCart);

                // When
                Cart result = cartDomainService.getOrCreateCart(memberId, storeId);

                // Then
                assertThat(result).isNotNull();
                then(cartRepository).should(times(1)).findByMemberIdAndStoreId(memberId, storeId);
                then(cartRepository).should(times(1)).save(any(Cart.class));
            }
        }
    }

    @Nested
    @DisplayName("clearCart 메서드는")
    class Describe_clearCart {

        @Nested
        @DisplayName("장바구니가 존재하고 비어있지 않으면")
        class Context_with_existing_non_empty_cart {

            @Test
            @DisplayName("장바구니를 비우고 저장한다")
            void it_clears_and_saves_cart() {
                // Given
                Long memberId = 1L;
                Long storeId = 100L;

                Cart cart = Cart.create(memberId, storeId);

                given(cartRepository.findByMemberIdAndStoreId(memberId, storeId))
                        .willReturn(Optional.of(cart));

                // When
                cartDomainService.clearCart(memberId, storeId);

                // Then
                then(cartRepository).should(times(1)).findByMemberIdAndStoreId(memberId, storeId);
            }
        }

        @Nested
        @DisplayName("장바구니가 존재하지 않으면")
        class Context_with_no_existing_cart {

            @Test
            @DisplayName("아무 작업도 수행하지 않는다")
            void it_does_nothing() {
                // Given
                Long memberId = 1L;
                Long storeId = 100L;

                given(cartRepository.findByMemberIdAndStoreId(memberId, storeId))
                        .willReturn(Optional.empty());

                // When
                cartDomainService.clearCart(memberId, storeId);

                // Then
                then(cartRepository).should(times(1)).findByMemberIdAndStoreId(memberId, storeId);
                then(cartRepository).should(times(0)).save(any(Cart.class));
                then(cartRepository).should(times(0)).delete(any(Cart.class));
            }
        }
    }

    @Nested
    @DisplayName("clearOtherCarts 메서드는")
    class Describe_clearOtherCarts {

        @Nested
        @DisplayName("특정 가게를 제외한 다른 장바구니 삭제가 요청되면")
        class Context_with_request_to_clear_other_carts {

            @Test
            @DisplayName("지정된 가게 외의 장바구니를 삭제한다")
            void it_deletes_other_carts() {
                // Given
                Long memberId = 1L;
                Long keepStoreId = 100L;

                // When
                cartDomainService.clearOtherCarts(memberId, keepStoreId);

                // Then
                then(cartRepository).should(times(1))
                        .deleteByMemberIdAndStoreIdNot(memberId, keepStoreId);
            }
        }
    }
}

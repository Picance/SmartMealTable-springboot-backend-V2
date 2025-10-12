package com.stdev.smartmealtable.storage.db.cart;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 장바구니 JPA Repository
 */
public interface CartJpaRepository extends JpaRepository<CartEntity, Long> {
    
    /**
     * 회원 ID와 가게 ID로 장바구니 조회 (CartItem 포함)
     */
    @Query("SELECT c FROM CartEntity c LEFT JOIN FETCH c.items WHERE c.memberId = :memberId AND c.storeId = :storeId")
    Optional<CartEntity> findByMemberIdAndStoreIdWithItems(@Param("memberId") Long memberId, 
                                                             @Param("storeId") Long storeId);
    
    /**
     * 회원 ID로 모든 장바구니 조회 (CartItem 포함)
     */
    @Query("SELECT c FROM CartEntity c LEFT JOIN FETCH c.items WHERE c.memberId = :memberId")
    List<CartEntity> findByMemberIdWithItems(@Param("memberId") Long memberId);
    
    /**
     * 회원 ID와 가게 ID로 장바구니 존재 여부 확인
     */
    boolean existsByMemberIdAndStoreId(Long memberId, Long storeId);
    
    /**
     * 회원의 특정 가게를 제외한 다른 장바구니 삭제
     */
    @Modifying
    @Query("DELETE FROM CartEntity c WHERE c.memberId = :memberId AND c.storeId <> :storeId")
    void deleteByMemberIdAndStoreIdNot(@Param("memberId") Long memberId, 
                                        @Param("storeId") Long storeId);
}

package com.stdev.smartmealtable.storage.db.store;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 가게 이미지 JPA Repository
 */
public interface StoreImageJpaRepository extends JpaRepository<StoreImageJpaEntity, Long> {
    
    /**
     * 가게의 모든 이미지 삭제
     */
    @Modifying
    @Query("DELETE FROM StoreImageJpaEntity s WHERE s.storeId = :storeId")
    void deleteByStoreId(@Param("storeId") Long storeId);
    
    /**
     * 가게의 모든 이미지 조회 (isMain 내림차순, displayOrder 오름차순 정렬)
     */
    List<StoreImageJpaEntity> findByStoreIdOrderByIsMainDescDisplayOrderAsc(Long storeId);
    
    /**
     * 가게의 대표 이미지 조회
     */
    Optional<StoreImageJpaEntity> findByStoreIdAndIsMainTrue(Long storeId);
    
    /**
     * 가게의 첫 번째 이미지 조회 (displayOrder 오름차순)
     */
    Optional<StoreImageJpaEntity> findFirstByStoreIdOrderByDisplayOrderAsc(Long storeId);
}

package com.stdev.smartmealtable.storage.db.store;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}

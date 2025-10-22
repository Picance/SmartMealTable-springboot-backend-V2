package com.stdev.smartmealtable.storage.db.store;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * StoreTemporaryClosure Spring Data JPA Repository
 */
public interface StoreTemporaryClosureJpaRepository extends JpaRepository<StoreTemporaryClosureJpaEntity, Long> {
    
    /**
     * 가게 ID와 휴무 날짜로 임시 휴무 목록 조회
     */
    @Query("SELECT s FROM StoreTemporaryClosureJpaEntity s WHERE s.storeId = :storeId AND s.closureDate = :closureDate")
    List<StoreTemporaryClosureJpaEntity> findByStoreIdAndClosureDate(
            @Param("storeId") Long storeId,
            @Param("closureDate") LocalDate closureDate
    );
    
    /**
     * 가게 ID로 모든 임시 휴무 목록 조회
     */
    @Query("SELECT s FROM StoreTemporaryClosureJpaEntity s WHERE s.storeId = :storeId")
    List<StoreTemporaryClosureJpaEntity> findByStoreId(@Param("storeId") Long storeId);
}

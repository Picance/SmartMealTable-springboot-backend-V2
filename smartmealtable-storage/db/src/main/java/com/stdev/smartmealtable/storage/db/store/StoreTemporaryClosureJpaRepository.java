package com.stdev.smartmealtable.storage.db.store;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * StoreTemporaryClosure Spring Data JPA Repository
 */
public interface StoreTemporaryClosureJpaRepository extends JpaRepository<StoreTemporaryClosureJpaEntity, Long> {
    
    /**
     * 가게 ID와 휴무 날짜로 임시 휴무 목록 조회
     */
    List<StoreTemporaryClosureJpaEntity> findByStoreIdAndClosureDate(Long storeId, LocalDate closureDate);
    
    /**
     * 가게 ID로 모든 임시 휴무 목록 조회
     */
    List<StoreTemporaryClosureJpaEntity> findByStoreId(Long storeId);
}

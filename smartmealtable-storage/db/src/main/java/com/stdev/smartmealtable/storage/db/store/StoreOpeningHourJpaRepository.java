package com.stdev.smartmealtable.storage.db.store;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * StoreOpeningHour Spring Data JPA Repository
 */
public interface StoreOpeningHourJpaRepository extends JpaRepository<StoreOpeningHourJpaEntity, Long> {
    
    /**
     * 가게 ID로 영업시간 목록 조회
     */
    List<StoreOpeningHourJpaEntity> findByStoreId(Long storeId);
}

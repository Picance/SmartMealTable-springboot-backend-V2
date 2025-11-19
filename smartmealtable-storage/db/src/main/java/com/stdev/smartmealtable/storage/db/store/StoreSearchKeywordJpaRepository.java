package com.stdev.smartmealtable.storage.db.store;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 가게 검색 키워드 JPA Repository.
 */
public interface StoreSearchKeywordJpaRepository extends JpaRepository<StoreSearchKeywordJpaEntity, Long> {

    void deleteByStoreId(Long storeId);
}

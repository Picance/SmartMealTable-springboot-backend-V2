package com.stdev.smartmealtable.storage.db.store;

import com.stdev.smartmealtable.domain.store.StoreViewHistory;
import com.stdev.smartmealtable.domain.store.StoreViewHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * StoreViewHistoryRepository 구현체
 */
@Repository
@RequiredArgsConstructor
public class StoreViewHistoryRepositoryImpl implements StoreViewHistoryRepository {
    
    private final StoreViewHistoryJpaRepository jpaRepository;
    
    @Override
    public StoreViewHistory save(StoreViewHistory storeViewHistory) {
        StoreViewHistoryJpaEntity entity = StoreEntityMapper.toJpaEntity(storeViewHistory);
        StoreViewHistoryJpaEntity saved = jpaRepository.save(entity);
        return StoreEntityMapper.toDomain(saved);
    }
}

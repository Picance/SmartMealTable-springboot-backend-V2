package com.stdev.smartmealtable.storage.db.store;

import com.stdev.smartmealtable.domain.store.StoreOpeningHour;
import com.stdev.smartmealtable.domain.store.StoreOpeningHourRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * StoreOpeningHourRepository 구현체
 */
@Repository
@RequiredArgsConstructor
public class StoreOpeningHourRepositoryImpl implements StoreOpeningHourRepository {
    
    private final StoreOpeningHourJpaRepository jpaRepository;
    
    @Override
    public List<StoreOpeningHour> findByStoreId(Long storeId) {
        return jpaRepository.findByStoreId(storeId)
                .stream()
                .map(StoreEntityMapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public StoreOpeningHour save(StoreOpeningHour storeOpeningHour) {
        StoreOpeningHourJpaEntity entity = StoreEntityMapper.toJpaEntity(storeOpeningHour);
        StoreOpeningHourJpaEntity saved = jpaRepository.save(entity);
        return StoreEntityMapper.toDomain(saved);
    }
}

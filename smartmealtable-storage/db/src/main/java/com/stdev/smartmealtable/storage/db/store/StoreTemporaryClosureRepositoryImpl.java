package com.stdev.smartmealtable.storage.db.store;

import com.stdev.smartmealtable.domain.store.StoreTemporaryClosure;
import com.stdev.smartmealtable.domain.store.StoreTemporaryClosureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * StoreTemporaryClosureRepository 구현체
 */
@Repository
@RequiredArgsConstructor
public class StoreTemporaryClosureRepositoryImpl implements StoreTemporaryClosureRepository {
    
    private final StoreTemporaryClosureJpaRepository jpaRepository;
    
    @Override
    public List<StoreTemporaryClosure> findByStoreIdAndClosureDate(Long storeId, LocalDate closureDate) {
        return jpaRepository.findByStoreIdAndClosureDate(storeId, closureDate)
                .stream()
                .map(StoreEntityMapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<StoreTemporaryClosure> findByStoreId(Long storeId) {
        return jpaRepository.findByStoreId(storeId)
                .stream()
                .map(StoreEntityMapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public StoreTemporaryClosure save(StoreTemporaryClosure storeTemporaryClosure) {
        StoreTemporaryClosureJpaEntity entity = StoreEntityMapper.toJpaEntity(storeTemporaryClosure);
        StoreTemporaryClosureJpaEntity saved = jpaRepository.save(entity);
        return StoreEntityMapper.toDomain(saved);
    }
}

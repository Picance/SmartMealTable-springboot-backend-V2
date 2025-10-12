package com.stdev.smartmealtable.storage.db.store;

import com.stdev.smartmealtable.domain.store.Store;
import com.stdev.smartmealtable.domain.store.StoreRepository;
import com.stdev.smartmealtable.domain.store.StoreType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * StoreRepository 구현체
 * Domain Repository → JPA Repository 어댑터
 */
@Repository
@RequiredArgsConstructor
public class StoreRepositoryImpl implements StoreRepository {
    
    private final StoreJpaRepository jpaRepository;
    private final StoreQueryDslRepository queryDslRepository;
    
    @Override
    public Optional<Store> findById(Long storeId) {
        return jpaRepository.findById(storeId)
                .map(StoreEntityMapper::toDomain);
    }
    
    @Override
    public Optional<Store> findByIdAndDeletedAtIsNull(Long storeId) {
        return jpaRepository.findByStoreIdAndDeletedAtIsNull(storeId)
                .map(StoreEntityMapper::toDomain);
    }
    
    @Override
    public List<Store> findByIdIn(List<Long> storeIds) {
        return jpaRepository.findByStoreIdInAndDeletedAtIsNull(storeIds)
                .stream()
                .map(StoreEntityMapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public Store save(Store store) {
        StoreJpaEntity entity = StoreEntityMapper.toJpaEntity(store);
        StoreJpaEntity saved = jpaRepository.save(entity);
        return StoreEntityMapper.toDomain(saved);
    }
    
    @Override
    public List<Store> searchByKeywordForAutocomplete(String keyword, int limit) {
        return jpaRepository.searchByKeywordForAutocomplete(keyword, limit)
                .stream()
                .map(StoreEntityMapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public StoreSearchResult searchStores(
            String keyword,
            BigDecimal userLatitude,
            BigDecimal userLongitude,
            Double radiusKm,
            Long categoryId,
            Boolean isOpenOnly,
            StoreType storeType,
            String sortBy,
            int page,
            int size
    ) {
        return queryDslRepository.searchStores(
                keyword,
                userLatitude,
                userLongitude,
                radiusKm,
                categoryId,
                isOpenOnly,
                storeType,
                sortBy,
                page,
                size
        );
    }
}

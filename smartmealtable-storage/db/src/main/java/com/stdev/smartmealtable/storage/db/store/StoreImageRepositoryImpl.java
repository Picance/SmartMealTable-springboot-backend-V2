package com.stdev.smartmealtable.storage.db.store;

import com.stdev.smartmealtable.domain.store.StoreImage;
import com.stdev.smartmealtable.domain.store.StoreImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * StoreImageRepository 구현체
 */
@Repository
@RequiredArgsConstructor
public class StoreImageRepositoryImpl implements StoreImageRepository {
    
    private final StoreImageJpaRepository jpaRepository;
    
    @Override
    @Transactional
    public StoreImage save(StoreImage storeImage) {
        StoreImageJpaEntity entity = StoreImageEntityMapper.toJpaEntity(storeImage);
        StoreImageJpaEntity saved = jpaRepository.save(entity);
        return StoreImageEntityMapper.toDomain(saved);
    }
    
    @Override
    public StoreImage findById(Long storeImageId) {
        return jpaRepository.findById(storeImageId)
                .map(StoreImageEntityMapper::toDomain)
                .orElse(null);
    }
    
    @Override
    @Transactional
    public void deleteByStoreId(Long storeId) {
        jpaRepository.deleteByStoreId(storeId);
    }
    
    @Override
    public List<StoreImage> findByStoreId(Long storeId) {
        return jpaRepository.findByStoreIdOrderByIsMainDescDisplayOrderAsc(storeId)
                .stream()
                .map(StoreImageEntityMapper::toDomain)
                .toList();
    }
    
    @Override
    public Optional<StoreImage> findByStoreIdAndIsMainTrue(Long storeId) {
        return jpaRepository.findByStoreIdAndIsMainTrue(storeId)
                .map(StoreImageEntityMapper::toDomain);
    }
    
    @Override
    public Optional<StoreImage> findFirstByStoreIdOrderByDisplayOrderAsc(Long storeId) {
        return jpaRepository.findFirstByStoreIdOrderByDisplayOrderAsc(storeId)
                .map(StoreImageEntityMapper::toDomain);
    }
}

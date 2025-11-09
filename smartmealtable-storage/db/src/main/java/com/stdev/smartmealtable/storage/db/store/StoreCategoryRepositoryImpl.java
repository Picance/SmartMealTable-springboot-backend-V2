package com.stdev.smartmealtable.storage.db.store;

import com.stdev.smartmealtable.domain.store.StoreCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * StoreCategoryRepository의 JPA 구현체
 * store_category 테이블과 상호작용
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class StoreCategoryRepositoryImpl implements StoreCategoryRepository {
    
    private final StoreCategoryJpaRepository jpaRepository;
    
    @Override
    public void save(Long storeId, Long categoryId, int displayOrder) {
        try {
            StoreCategoryJpaEntity entity = StoreCategoryJpaEntity.builder()
                    .storeId(storeId)
                    .categoryId(categoryId)
                    .displayOrder(displayOrder)
                    .build();
            
            jpaRepository.save(entity);
            log.debug("Saved store-category mapping: storeId={}, categoryId={}, displayOrder={}", 
                    storeId, categoryId, displayOrder);
        } catch (Exception e) {
            log.error("Failed to save store-category mapping: storeId={}, categoryId={}", storeId, categoryId, e);
            throw e;
        }
    }
    
    @Override
    public List<Long> findCategoryIdsByStoreId(Long storeId) {
        try {
            List<Long> categoryIds = jpaRepository.findCategoryIdsByStoreId(storeId);
            log.debug("Found {} categories for store ID: {}", categoryIds.size(), storeId);
            return categoryIds;
        } catch (Exception e) {
            log.error("Failed to find categories for store ID: {}", storeId, e);
            throw e;
        }
    }
    
    @Override
    public void deleteByStoreId(Long storeId) {
        try {
            jpaRepository.deleteByStoreId(storeId);
            log.debug("Deleted all store-category mappings for store ID: {}", storeId);
        } catch (Exception e) {
            log.error("Failed to delete store-category mappings for store ID: {}", storeId, e);
            throw e;
        }
    }
    
    @Override
    public boolean existsByStoreIdAndCategoryId(Long storeId, Long categoryId) {
        try {
            boolean exists = jpaRepository.existsByStoreIdAndCategoryId(storeId, categoryId);
            log.debug("Store-category mapping exists: storeId={}, categoryId={}, exists={}", storeId, categoryId, exists);
            return exists;
        } catch (Exception e) {
            log.error("Failed to check store-category mapping: storeId={}, categoryId={}", storeId, categoryId, e);
            throw e;
        }
    }
}

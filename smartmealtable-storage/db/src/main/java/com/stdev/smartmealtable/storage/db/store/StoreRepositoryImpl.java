package com.stdev.smartmealtable.storage.db.store;

import com.stdev.smartmealtable.domain.store.*;
import com.stdev.smartmealtable.storage.db.search.SearchKeywordSupport;
import com.stdev.smartmealtable.storage.db.search.SearchKeywordSupport.SearchKeyword;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    private final StoreCategoryJpaRepository storeCategoryJpaRepository;
    private final StoreQueryDslRepository queryDslRepository;
    private final StoreOpeningHourJpaRepository openingHourJpaRepository;
    private final StoreTemporaryClosureJpaRepository temporaryClosureJpaRepository;
    private final StoreSearchKeywordJpaRepository storeSearchKeywordJpaRepository;
    
    @Override
    public Optional<Store> findById(Long storeId) {
        return jpaRepository.findById(storeId)
                .map(entity -> {
                    List<Long> categoryIds = storeCategoryJpaRepository.findCategoryIdsByStoreId(storeId);
                    return StoreEntityMapper.toDomain(entity, categoryIds);
                });
    }
    
    @Override
    public Optional<Store> findByIdAndDeletedAtIsNull(Long storeId) {
        return jpaRepository.findByStoreIdAndDeletedAtIsNull(storeId)
                .map(entity -> {
                    List<Long> categoryIds = storeCategoryJpaRepository.findCategoryIdsByStoreId(storeId);
                    return StoreEntityMapper.toDomain(entity, categoryIds);
                });
    }
    
    @Override
    public Optional<Store> findByExternalId(String externalId) {
        return jpaRepository.findByExternalId(externalId)
                .map(entity -> {
                    List<Long> categoryIds = storeCategoryJpaRepository.findCategoryIdsByStoreId(entity.getStoreId());
                    return StoreEntityMapper.toDomain(entity, categoryIds);
                });
    }
    
    @Override
    public List<Store> findByIdIn(List<Long> storeIds) {
        return jpaRepository.findByStoreIdInAndDeletedAtIsNull(storeIds)
                .stream()
                .map(entity -> {
                    List<Long> categoryIds = storeCategoryJpaRepository.findCategoryIdsByStoreId(entity.getStoreId());
                    return StoreEntityMapper.toDomain(entity, categoryIds);
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public Store save(Store store) {
        StoreJpaEntity entity = StoreEntityMapper.toJpaEntity(store);
        StoreJpaEntity saved = jpaRepository.save(entity);
        
        // 기존 카테고리 매핑 제거
        storeCategoryJpaRepository.deleteByStoreId(saved.getStoreId());
        
        // 새로운 카테고리 매핑 생성
        List<Long> categoryIds = store.getCategoryIds();
        if (categoryIds != null && !categoryIds.isEmpty()) {
            for (int i = 0; i < categoryIds.size(); i++) {
                StoreCategoryJpaEntity mapping = StoreCategoryJpaEntity.builder()
                        .storeId(saved.getStoreId())
                        .categoryId(categoryIds.get(i))
                        .displayOrder(i)
                        .build();
                storeCategoryJpaRepository.save(mapping);
            }
        }

        refreshStoreSearchKeywords(saved);
        
        return StoreEntityMapper.toDomain(saved, categoryIds);
    }
    
    @Override
    public List<Store> searchByKeywordForAutocomplete(String keyword, int limit) {
        return jpaRepository.searchByKeywordForAutocomplete(keyword, limit)
                .stream()
                .map(entity -> {
                    List<Long> categoryIds = storeCategoryJpaRepository.findCategoryIdsByStoreId(entity.getStoreId());
                    return StoreEntityMapper.toDomain(entity, categoryIds);
                })
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

    // ===== ADMIN 전용 메서드 =====

    @Override
    public StorePageResult adminSearch(Long categoryId, String name, StoreType storeType, int page, int size) {
        return queryDslRepository.adminSearch(categoryId, name, storeType, page, size);
    }

    @Override
    public void softDelete(Long storeId) {
        jpaRepository.findById(storeId).ifPresent(store -> {
            StoreJpaEntity updated = StoreJpaEntity.builder()
                    .storeId(store.getStoreId())
                    .externalId(store.getExternalId())
                    .name(store.getName())
                    .sellerId(store.getSellerId())
                    .address(store.getAddress())
                    .lotNumberAddress(store.getLotNumberAddress())
                    .latitude(store.getLatitude())
                    .longitude(store.getLongitude())
                    .phoneNumber(store.getPhoneNumber())
                    .description(store.getDescription())
                    .averagePrice(store.getAveragePrice())
                    .reviewCount(store.getReviewCount())
                    .viewCount(store.getViewCount())
                    .favoriteCount(store.getFavoriteCount())
                    .storeType(store.getStoreType())
                    .imageUrl(store.getImageUrl())
                    .registeredAt(store.getRegisteredAt())
                    .deletedAt(LocalDateTime.now()) // 논리적 삭제
                    .build();
            jpaRepository.save(updated);
            storeSearchKeywordJpaRepository.deleteByStoreId(storeId);
        });
    }

    @Override
    public boolean existsByCategoryIdAndNotDeleted(Long categoryId) {
        return queryDslRepository.existsByCategoryIdAndNotDeleted(categoryId);
    }

    // ===== StoreOpeningHour 관련 =====

    @Override
    public StoreOpeningHour saveOpeningHour(StoreOpeningHour openingHour) {
        StoreOpeningHourJpaEntity entity = StoreEntityMapper.toJpaEntity(openingHour);
        StoreOpeningHourJpaEntity saved = openingHourJpaRepository.save(entity);
        return StoreEntityMapper.toDomain(saved);
    }

    @Override
    public List<StoreOpeningHour> findOpeningHoursByStoreId(Long storeId) {
        return openingHourJpaRepository.findByStoreId(storeId).stream()
                .map(StoreEntityMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<StoreOpeningHour> findOpeningHourById(Long storeOpeningHourId) {
        return openingHourJpaRepository.findById(storeOpeningHourId)
                .map(StoreEntityMapper::toDomain);
    }

    @Override
    public void deleteOpeningHourById(Long storeOpeningHourId) {
        openingHourJpaRepository.deleteById(storeOpeningHourId);
    }

    // ===== StoreTemporaryClosure 관련 =====

    @Override
    public StoreTemporaryClosure saveTemporaryClosure(StoreTemporaryClosure temporaryClosure) {
        StoreTemporaryClosureJpaEntity entity = StoreEntityMapper.toJpaEntity(temporaryClosure);
        StoreTemporaryClosureJpaEntity saved = temporaryClosureJpaRepository.save(entity);
        return StoreEntityMapper.toDomain(saved);
    }

    @Override
    public List<StoreTemporaryClosure> findTemporaryClosuresByStoreId(Long storeId) {
        return temporaryClosureJpaRepository.findByStoreId(storeId).stream()
                .map(StoreEntityMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<StoreTemporaryClosure> findTemporaryClosureById(Long storeTemporaryClosureId) {
        return temporaryClosureJpaRepository.findById(storeTemporaryClosureId)
                .map(StoreEntityMapper::toDomain);
    }

    @Override
    public void deleteTemporaryClosureById(Long storeTemporaryClosureId) {
        temporaryClosureJpaRepository.deleteById(storeTemporaryClosureId);
    }
    
    // ===== 자동완성 전용 메서드 (Phase 3) =====
    
    @Override
    public List<Store> findByNameStartsWith(String prefix, int limit) {
        return queryDslRepository.findByNameStartingWith(prefix, limit)
                .stream()
                .map(entity -> {
                    List<Long> categoryIds = storeCategoryJpaRepository.findCategoryIdsByStoreId(entity.getStoreId());
                    return StoreEntityMapper.toDomain(entity, categoryIds);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Store> findByNameContains(String keyword, int limit) {
        return queryDslRepository.findByNameContaining(keyword, limit)
                .stream()
                .map(entity -> {
                    List<Long> categoryIds = storeCategoryJpaRepository.findCategoryIdsByStoreId(entity.getStoreId());
                    return StoreEntityMapper.toDomain(entity, categoryIds);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Store> findAllByIdIn(List<Long> storeIds) {
        return queryDslRepository.findByStoreIdIn(storeIds)
                .stream()
                .map(entity -> {
                    List<Long> categoryIds = storeCategoryJpaRepository.findCategoryIdsByStoreId(entity.getStoreId());
                    return StoreEntityMapper.toDomain(entity, categoryIds);
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public long count() {
        return jpaRepository.count();
    }
    
    @Override
    public List<Store> findAll(int page, int size) {
        return jpaRepository.findAll(
                        org.springframework.data.domain.PageRequest.of(page, size)
                ).getContent()
                .stream()
                .map(entity -> {
                    List<Long> categoryIds = storeCategoryJpaRepository.findCategoryIdsByStoreId(entity.getStoreId());
                    return StoreEntityMapper.toDomain(entity, categoryIds);
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Store> findAllWithCategories() {
        return jpaRepository.findAll()
                .stream()
                .map(entity -> {
                    List<Long> categoryIds = storeCategoryJpaRepository.findCategoryIdsByStoreId(entity.getStoreId());
                    return StoreEntityMapper.toDomain(entity, categoryIds);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<StoreWithDistance> findByDistanceOrderByDistance(
            double userLatitude,
            double userLongitude,
            double radiusKm,
            int limit
    ) {
        return queryDslRepository.findByDistanceOrderByDistance(
                userLatitude,
                userLongitude,
                radiusKm,
                limit
        );
    }

    @Override
    public List<Store> findByPopularity(int limit) {
        return queryDslRepository.findByPopularity(limit)
                .stream()
                .map(entity -> {
                    List<Long> categoryIds = storeCategoryJpaRepository.findCategoryIdsByStoreId(entity.getStoreId());
                    return StoreEntityMapper.toDomain(entity, categoryIds);
                })
                .collect(Collectors.toList());
    }

    private void refreshStoreSearchKeywords(StoreJpaEntity saved) {
        if (saved == null || saved.getStoreId() == null) {
            return;
        }

        storeSearchKeywordJpaRepository.deleteByStoreId(saved.getStoreId());

        List<SearchKeyword> keywords = SearchKeywordSupport.generateKeywords(saved.getName());
        if (keywords.isEmpty()) {
            return;
        }

        List<StoreSearchKeywordJpaEntity> keywordEntities = keywords.stream()
                .map(keyword -> StoreSearchKeywordJpaEntity.of(
                        saved.getStoreId(),
                        keyword.keyword(),
                        keyword.keywordPrefix(),
                        StoreSearchKeywordType.NAME_SUBSTRING
                ))
                .collect(Collectors.toList());

        storeSearchKeywordJpaRepository.saveAll(keywordEntities);
    }
}

package com.stdev.smartmealtable.storage.db.food;

import com.stdev.smartmealtable.domain.food.Food;
import com.stdev.smartmealtable.domain.food.FoodPageResult;
import com.stdev.smartmealtable.domain.food.FoodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Food Repository 구현체
 */
@Repository
@RequiredArgsConstructor
public class FoodRepositoryImpl implements FoodRepository {

    private final FoodJpaRepository foodJpaRepository;

    @Override
    public Food save(Food food) {
        FoodJpaEntity entity = FoodJpaEntity.fromDomain(food);
        FoodJpaEntity savedEntity = foodJpaRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public Optional<Food> findById(Long foodId) {
        return foodJpaRepository.findById(foodId)
                .map(FoodJpaEntity::toDomain);
    }

    @Override
    public List<Food> findByIdIn(List<Long> foodIds) {
        return foodJpaRepository.findByFoodIdIn(foodIds).stream()
                .map(FoodJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Food> findAll(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<FoodJpaEntity> pageResult = foodJpaRepository.findAll(pageRequest);
        return pageResult.getContent().stream()
                .map(FoodJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Food> findRandom(int page, int size) {
        return foodJpaRepository.findRandom(page, size).stream()
                .map(FoodJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Food> findByCategoryId(Long categoryId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<FoodJpaEntity> pageResult = foodJpaRepository.findByCategoryId(categoryId, pageRequest);
        return pageResult.getContent().stream()
                .map(FoodJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long count() {
        return foodJpaRepository.count();
    }

    @Override
    public long countByCategoryId(Long categoryId) {
        return foodJpaRepository.countByCategoryId(categoryId);
    }

    @Override
    public List<Food> findByStoreId(Long storeId) {
        return foodJpaRepository.findByStoreId(storeId).stream()
                .map(FoodJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteByStoreId(Long storeId) {
        foodJpaRepository.deleteByStoreId(storeId);
    }

    // ===== ADMIN 전용 메서드 =====

    @Override
    public Optional<Food> findByIdAndDeletedAtIsNull(Long foodId) {
        return foodJpaRepository.findByFoodIdAndDeletedAtIsNull(foodId)
                .map(FoodJpaEntity::toDomain);
    }

    @Override
    public FoodPageResult adminSearch(Long categoryId, Long storeId, String name, int page, int size) {
        return foodJpaRepository.adminSearch(categoryId, storeId, name, page, size);
    }

    @Override
    public void softDelete(Long foodId) {
        foodJpaRepository.findById(foodId).ifPresent(food -> {
            FoodJpaEntity updated = FoodJpaEntity.builder()
                    .foodId(food.getFoodId())
                    .storeId(food.getStoreId())
                    .foodName(food.getFoodName())
                    .price(food.getPrice())
                    .categoryId(food.getCategoryId())
                    .description(food.getDescription())
                    .imageUrl(food.getImageUrl())
                    .registeredDt(food.getRegisteredDt())
                    .deletedAt(LocalDateTime.now()) // 논리적 삭제
                    .build();
            foodJpaRepository.save(updated);
        });
    }

    @Override
    public boolean existsByCategoryIdAndNotDeleted(Long categoryId) {
        return foodJpaRepository.existsByCategoryIdAndNotDeleted(categoryId);
    }

    @Override
    public boolean existsByStoreIdAndNotDeleted(Long storeId) {
        return foodJpaRepository.existsByStoreIdAndNotDeleted(storeId);
    }
    
    // ===== 자동완성 전용 메서드 (Phase 3) =====
    
    @Override
    public List<Food> findByNameStartsWith(String prefix, int limit) {
        return foodJpaRepository.findByNameStartingWith(prefix, limit)
                .stream()
                .map(FoodJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Food> findByNameContains(String keyword, int limit) {
        return foodJpaRepository.findByNameContaining(keyword, limit)
                .stream()
                .map(FoodJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Food> findAllByIdIn(List<Long> foodIds) {
        return foodJpaRepository.findByFoodIdIn(foodIds)
                .stream()
                .map(FoodJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Food> findAllWithCategories() {
        return foodJpaRepository.findAll()
                .stream()
                .map(FoodJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Food> findByStoreIdOrderByDistance(
            Long storeId,
            double userLatitude,
            double userLongitude,
            int limit
    ) {
        return foodJpaRepository.findByStoreIdOrderByDistance(storeId, userLatitude, userLongitude, limit)
                .stream()
                .map(FoodJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Food> findByPopularity(int limit) {
        return foodJpaRepository.findByPopularity(limit)
                .stream()
                .map(FoodJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

}

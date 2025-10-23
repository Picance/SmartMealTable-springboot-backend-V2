package com.stdev.smartmealtable.storage.db.food;

import com.stdev.smartmealtable.domain.food.Food;
import com.stdev.smartmealtable.domain.food.FoodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

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
}

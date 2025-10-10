package com.stdev.smartmealtable.storage.db.food;

import com.stdev.smartmealtable.domain.food.FoodPreference;
import com.stdev.smartmealtable.domain.food.FoodPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * FoodPreference Repository 구현체
 */
@Repository
@RequiredArgsConstructor
public class FoodPreferenceRepositoryImpl implements FoodPreferenceRepository {

    private final FoodPreferenceJpaRepository foodPreferenceJpaRepository;

    @Override
    public FoodPreference save(FoodPreference foodPreference) {
        FoodPreferenceJpaEntity entity = FoodPreferenceJpaEntity.fromDomain(foodPreference);
        FoodPreferenceJpaEntity savedEntity = foodPreferenceJpaRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public List<FoodPreference> findByMemberId(Long memberId) {
        return foodPreferenceJpaRepository.findByMemberId(memberId).stream()
                .map(FoodPreferenceJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<FoodPreference> findByMemberIdAndFoodId(Long memberId, Long foodId) {
        return foodPreferenceJpaRepository.findByMemberIdAndFoodId(memberId, foodId)
                .map(FoodPreferenceJpaEntity::toDomain);
    }

    @Override
    public Optional<FoodPreference> findById(Long foodPreferenceId) {
        return foodPreferenceJpaRepository.findById(foodPreferenceId)
                .map(FoodPreferenceJpaEntity::toDomain);
    }

    @Override
    public List<FoodPreference> findByMemberIdAndIsPreferred(Long memberId, Boolean isPreferred) {
        return foodPreferenceJpaRepository.findByMemberIdAndIsPreferred(memberId, isPreferred).stream()
                .map(FoodPreferenceJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByMemberId(Long memberId) {
        foodPreferenceJpaRepository.deleteByMemberId(memberId);
    }

    @Override
    public void deleteByMemberIdAndFoodId(Long memberId, Long foodId) {
        foodPreferenceJpaRepository.deleteByMemberIdAndFoodId(memberId, foodId);
    }

    @Override
    public void deleteById(Long foodPreferenceId) {
        foodPreferenceJpaRepository.deleteById(foodPreferenceId);
    }

    @Override
    public long countByMemberIdAndIsPreferred(Long memberId, Boolean isPreferred) {
        return foodPreferenceJpaRepository.countByMemberIdAndIsPreferred(memberId, isPreferred);
    }
}

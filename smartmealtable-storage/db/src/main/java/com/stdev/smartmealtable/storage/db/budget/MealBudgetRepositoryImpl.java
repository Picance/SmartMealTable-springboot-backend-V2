package com.stdev.smartmealtable.storage.db.budget;

import com.stdev.smartmealtable.domain.budget.MealBudget;
import com.stdev.smartmealtable.domain.budget.MealBudgetRepository;
import com.stdev.smartmealtable.domain.budget.MealType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MealBudgetRepository 구현체
 */
@Repository
@RequiredArgsConstructor
public class MealBudgetRepositoryImpl implements MealBudgetRepository {

    private final MealBudgetJpaRepository jpaRepository;

    @Override
    public MealBudget save(MealBudget mealBudget) {
        MealBudgetJpaEntity entity = MealBudgetJpaEntity.from(mealBudget);
        MealBudgetJpaEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public List<MealBudget> findByDailyBudgetId(Long dailyBudgetId) {
        return jpaRepository.findByDailyBudgetId(dailyBudgetId).stream()
                .map(MealBudgetJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public MealBudget findByDailyBudgetIdAndMealType(Long dailyBudgetId, MealType mealType) {
        return jpaRepository.findByDailyBudgetIdAndMealType(dailyBudgetId, mealType)
                .map(MealBudgetJpaEntity::toDomain)
                .orElse(null);
    }

    @Override
    public List<MealBudget> findByMemberIdAndBudgetDate(Long memberId, LocalDate budgetDate) {
        return jpaRepository.findByMemberIdAndBudgetDate(memberId, budgetDate).stream()
                .map(MealBudgetJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
}

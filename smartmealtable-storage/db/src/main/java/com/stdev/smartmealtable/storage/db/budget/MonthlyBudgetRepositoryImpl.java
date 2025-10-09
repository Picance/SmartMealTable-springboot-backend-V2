package com.stdev.smartmealtable.storage.db.budget;

import com.stdev.smartmealtable.domain.budget.MonthlyBudget;
import com.stdev.smartmealtable.domain.budget.MonthlyBudgetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * MonthlyBudgetRepository 구현체
 */
@Repository
@RequiredArgsConstructor
public class MonthlyBudgetRepositoryImpl implements MonthlyBudgetRepository {

    private final MonthlyBudgetJpaRepository jpaRepository;

    @Override
    public MonthlyBudget save(MonthlyBudget monthlyBudget) {
        MonthlyBudgetJpaEntity entity = MonthlyBudgetJpaEntity.from(monthlyBudget);
        MonthlyBudgetJpaEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<MonthlyBudget> findByMemberIdAndBudgetMonth(Long memberId, String budgetMonth) {
        return jpaRepository.findByMemberIdAndBudgetMonth(memberId, budgetMonth)
                .map(MonthlyBudgetJpaEntity::toDomain);
    }

    @Override
    public Optional<MonthlyBudget> findLatestByMemberId(Long memberId) {
        return jpaRepository.findLatestByMemberId(memberId)
                .map(MonthlyBudgetJpaEntity::toDomain);
    }
}

package com.stdev.smartmealtable.storage.db.budget;

import com.stdev.smartmealtable.domain.budget.DailyBudget;
import com.stdev.smartmealtable.domain.budget.DailyBudgetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * DailyBudgetRepository 구현체
 */
@Repository
@RequiredArgsConstructor
public class DailyBudgetRepositoryImpl implements DailyBudgetRepository {

    private final DailyBudgetJpaRepository jpaRepository;

    @Override
    public DailyBudget save(DailyBudget dailyBudget) {
        DailyBudgetJpaEntity entity = DailyBudgetJpaEntity.from(dailyBudget);
        DailyBudgetJpaEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<DailyBudget> findByMemberIdAndBudgetDate(Long memberId, LocalDate budgetDate) {
        return jpaRepository.findByMemberIdAndBudgetDate(memberId, budgetDate)
                .map(DailyBudgetJpaEntity::toDomain);
    }

    @Override
    public Optional<DailyBudget> findLatestByMemberId(Long memberId) {
        return jpaRepository.findLatestByMemberId(memberId)
                .map(DailyBudgetJpaEntity::toDomain);
    }
}

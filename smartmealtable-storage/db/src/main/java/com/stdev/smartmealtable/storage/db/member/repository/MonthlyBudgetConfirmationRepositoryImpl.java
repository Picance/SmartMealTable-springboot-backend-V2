package com.stdev.smartmealtable.storage.db.member.repository;

import com.stdev.smartmealtable.domain.member.entity.MonthlyBudgetConfirmation;
import com.stdev.smartmealtable.domain.member.repository.MonthlyBudgetConfirmationRepository;
import com.stdev.smartmealtable.storage.db.member.entity.MonthlyBudgetConfirmationJpaEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 월별 예산 확인 이력 Repository 구현체
 * Domain Repository 인터페이스를 구현하여 JPA와 Domain을 연결
 */
@Repository
public class MonthlyBudgetConfirmationRepositoryImpl implements MonthlyBudgetConfirmationRepository {

    private final MonthlyBudgetConfirmationJpaRepository jpaRepository;

    public MonthlyBudgetConfirmationRepositoryImpl(MonthlyBudgetConfirmationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public MonthlyBudgetConfirmation save(MonthlyBudgetConfirmation confirmation) {
        MonthlyBudgetConfirmationJpaEntity jpaEntity = MonthlyBudgetConfirmationJpaEntity.from(confirmation);
        MonthlyBudgetConfirmationJpaEntity saved = jpaRepository.save(jpaEntity);
        return saved.toDomain();
    }

    @Override
    public Optional<MonthlyBudgetConfirmation> findByMemberIdAndYearAndMonth(Long memberId, Integer year, Integer month) {
        return jpaRepository.findByMemberIdAndYearAndMonth(memberId, year, month)
                .map(MonthlyBudgetConfirmationJpaEntity::toDomain);
    }

    @Override
    public boolean existsByMemberIdAndYearAndMonth(Long memberId, Integer year, Integer month) {
        return jpaRepository.existsByMemberIdAndYearAndMonth(memberId, year, month);
    }
}

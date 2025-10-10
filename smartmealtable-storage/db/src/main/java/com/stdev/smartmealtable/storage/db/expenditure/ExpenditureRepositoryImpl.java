package com.stdev.smartmealtable.storage.db.expenditure;

import com.stdev.smartmealtable.domain.expenditure.Expenditure;
import com.stdev.smartmealtable.domain.expenditure.ExpenditureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ExpenditureRepository 구현체
 * Domain Repository → JPA Repository 어댑터
 */
@Repository
@RequiredArgsConstructor
public class ExpenditureRepositoryImpl implements ExpenditureRepository {
    
    private final ExpenditureJpaRepository jpaRepository;
    
    @Override
    public Expenditure save(Expenditure expenditure) {
        ExpenditureJpaEntity entity = ExpenditureJpaEntity.from(expenditure);
        ExpenditureJpaEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }
    
    @Override
    public Optional<Expenditure> findById(Long expenditureId) {
        return jpaRepository.findById(expenditureId)
                .map(ExpenditureJpaEntity::toDomain);
    }
    
    @Override
    public Optional<Expenditure> findByIdAndNotDeleted(Long expenditureId) {
        return jpaRepository.findByIdAndNotDeleted(expenditureId)
                .map(ExpenditureJpaEntity::toDomain);
    }
    
    @Override
    public List<Expenditure> findByMemberIdAndDateRange(Long memberId, LocalDate startDate, LocalDate endDate) {
        return jpaRepository.findByMemberIdAndDateRange(memberId, startDate, endDate)
                .stream()
                .map(ExpenditureJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public Optional<Expenditure> findByIdAndMemberId(Long expenditureId, Long memberId) {
        return jpaRepository.findByIdAndMemberId(expenditureId, memberId)
                .map(ExpenditureJpaEntity::toDomain);
    }
    
    @Override
    public void delete(Expenditure expenditure) {
        ExpenditureJpaEntity entity = ExpenditureJpaEntity.from(expenditure);
        jpaRepository.delete(entity);
    }
    
    @Override
    public boolean existsByMemberIdAndMonth(Long memberId, int year, int month) {
        return jpaRepository.existsByMemberIdAndMonth(memberId, year, month);
    }
}

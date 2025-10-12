package com.stdev.smartmealtable.storage.db.expenditure;

import com.stdev.smartmealtable.domain.expenditure.Expenditure;
import com.stdev.smartmealtable.domain.expenditure.ExpenditureRepository;
import com.stdev.smartmealtable.domain.expenditure.MealType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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
    
    @Override
    public Long getTotalAmountByPeriod(Long memberId, LocalDate startDate, LocalDate endDate) {
        return jpaRepository.getTotalAmountByPeriod(memberId, startDate, endDate);
    }
    
    @Override
    public Map<Long, Long> getAmountByCategoryForPeriod(Long memberId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> raw = jpaRepository.getAmountByCategoryForPeriodRaw(memberId, startDate, endDate);
        return raw.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],  // categoryId
                        row -> (Long) row[1]   // amount
                ));
    }
    
    @Override
    public Map<LocalDate, Long> getDailyAmountForPeriod(Long memberId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> raw = jpaRepository.getDailyAmountForPeriodRaw(memberId, startDate, endDate);
        return raw.stream()
                .collect(Collectors.toMap(
                        row -> (LocalDate) row[0],  // expendedDate
                        row -> (Long) row[1]         // amount
                ));
    }
    
    @Override
    public Map<MealType, Long> getAmountByMealTypeForPeriod(Long memberId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> raw = jpaRepository.getAmountByMealTypeForPeriodRaw(memberId, startDate, endDate);
        return raw.stream()
                .collect(Collectors.toMap(
                        row -> (MealType) row[0],  // mealType
                        row -> (Long) row[1]        // amount
                ));
    }
}

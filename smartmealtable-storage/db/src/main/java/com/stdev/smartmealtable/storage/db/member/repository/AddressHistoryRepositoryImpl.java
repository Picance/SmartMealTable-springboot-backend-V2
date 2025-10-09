package com.stdev.smartmealtable.storage.db.member.repository;

import com.stdev.smartmealtable.domain.member.entity.AddressHistory;
import com.stdev.smartmealtable.domain.member.repository.AddressHistoryRepository;
import com.stdev.smartmealtable.storage.db.member.entity.AddressHistoryJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 주소 이력 Repository 구현체
 */
@Repository
@RequiredArgsConstructor
public class AddressHistoryRepositoryImpl implements AddressHistoryRepository {
    
    private final AddressHistoryJpaRepository jpaRepository;
    
    @Override
    public AddressHistory save(AddressHistory addressHistory) {
        AddressHistoryJpaEntity entity = AddressHistoryJpaEntity.from(addressHistory);
        AddressHistoryJpaEntity savedEntity = jpaRepository.save(entity);
        return savedEntity.toDomain();
    }
    
    @Override
    public Optional<AddressHistory> findById(Long addressHistoryId) {
        return jpaRepository.findById(addressHistoryId)
                .map(AddressHistoryJpaEntity::toDomain);
    }
    
    @Override
    public List<AddressHistory> findAllByMemberId(Long memberId) {
        return jpaRepository.findAllByMemberId(memberId)
                .stream()
                .map(AddressHistoryJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public Optional<AddressHistory> findPrimaryByMemberId(Long memberId) {
        return jpaRepository.findPrimaryByMemberId(memberId)
                .map(AddressHistoryJpaEntity::toDomain);
    }
    
    @Override
    public long countByMemberId(Long memberId) {
        return jpaRepository.countByMemberId(memberId);
    }
}

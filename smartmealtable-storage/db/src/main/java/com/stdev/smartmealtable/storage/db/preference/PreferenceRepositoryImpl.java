package com.stdev.smartmealtable.storage.db.preference;

import com.stdev.smartmealtable.domain.preference.Preference;
import com.stdev.smartmealtable.domain.preference.PreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 선호도 정보 Repository 구현체
 */
@Repository
@RequiredArgsConstructor
public class PreferenceRepositoryImpl implements PreferenceRepository {

    private final PreferenceJpaRepository preferenceJpaRepository;

    @Override
    public Preference save(Preference preference) {
        PreferenceJpaEntity entity;
        
        if (preference.getPreferenceId() == null) {
            // 신규 생성
            entity = PreferenceJpaEntity.fromDomain(preference);
        } else {
            // 기존 엔티티 업데이트
            entity = PreferenceJpaEntity.fromDomainWithId(preference);
        }
        
        PreferenceJpaEntity saved = preferenceJpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public List<Preference> findByMemberId(Long memberId) {
        return preferenceJpaRepository.findByMemberId(memberId).stream()
            .map(PreferenceJpaEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public Optional<Preference> findByMemberIdAndCategoryId(Long memberId, Long categoryId) {
        return preferenceJpaRepository.findByMemberIdAndCategoryId(memberId, categoryId)
            .map(PreferenceJpaEntity::toDomain);
    }

    @Override
    public void deleteByMemberId(Long memberId) {
        preferenceJpaRepository.deleteByMemberId(memberId);
    }
}

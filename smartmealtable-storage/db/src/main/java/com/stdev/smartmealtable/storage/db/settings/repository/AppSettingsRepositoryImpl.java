package com.stdev.smartmealtable.storage.db.settings.repository;

import com.stdev.smartmealtable.domain.settings.entity.AppSettings;
import com.stdev.smartmealtable.domain.settings.repository.AppSettingsRepository;
import com.stdev.smartmealtable.storage.db.settings.entity.AppSettingsJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 앱 설정 Repository 구현체
 * Domain Repository 인터페이스를 JPA로 구현
 */
@Repository
@RequiredArgsConstructor
public class AppSettingsRepositoryImpl implements AppSettingsRepository {

    private final AppSettingsJpaRepository jpaRepository;

    @Override
    public AppSettings save(AppSettings appSettings) {
        AppSettingsJpaEntity entity = AppSettingsJpaEntity.from(appSettings);
        AppSettingsJpaEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<AppSettings> findByMemberId(Long memberId) {
        return jpaRepository.findByMemberId(memberId)
                .map(AppSettingsJpaEntity::toDomain);
    }

    @Override
    public void delete(AppSettings appSettings) {
        AppSettingsJpaEntity entity = AppSettingsJpaEntity.from(appSettings);
        jpaRepository.delete(entity);
    }
}

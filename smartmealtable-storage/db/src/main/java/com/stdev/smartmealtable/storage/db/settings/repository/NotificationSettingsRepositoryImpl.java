package com.stdev.smartmealtable.storage.db.settings.repository;

import com.stdev.smartmealtable.domain.settings.entity.NotificationSettings;
import com.stdev.smartmealtable.domain.settings.repository.NotificationSettingsRepository;
import com.stdev.smartmealtable.storage.db.settings.entity.NotificationSettingsJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 알림 설정 Repository 구현체
 * Domain Repository 인터페이스를 JPA로 구현
 */
@Repository
@RequiredArgsConstructor
public class NotificationSettingsRepositoryImpl implements NotificationSettingsRepository {

    private final NotificationSettingsJpaRepository jpaRepository;

    @Override
    public NotificationSettings save(NotificationSettings notificationSettings) {
        NotificationSettingsJpaEntity entity = NotificationSettingsJpaEntity.from(notificationSettings);
        NotificationSettingsJpaEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<NotificationSettings> findByMemberId(Long memberId) {
        return jpaRepository.findByMemberId(memberId)
                .map(NotificationSettingsJpaEntity::toDomain);
    }

    @Override
    public void delete(NotificationSettings notificationSettings) {
        NotificationSettingsJpaEntity entity = NotificationSettingsJpaEntity.from(notificationSettings);
        jpaRepository.delete(entity);
    }
}

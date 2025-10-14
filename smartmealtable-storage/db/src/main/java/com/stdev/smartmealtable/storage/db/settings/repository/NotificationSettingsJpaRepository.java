package com.stdev.smartmealtable.storage.db.settings.repository;

import com.stdev.smartmealtable.storage.db.settings.entity.NotificationSettingsJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 알림 설정 JPA Repository
 */
public interface NotificationSettingsJpaRepository extends JpaRepository<NotificationSettingsJpaEntity, Long> {
    
    /**
     * 회원 ID로 알림 설정 조회
     * 
     * @param memberId 회원 ID
     * @return 알림 설정 JPA Entity (Optional)
     */
    Optional<NotificationSettingsJpaEntity> findByMemberId(Long memberId);
}

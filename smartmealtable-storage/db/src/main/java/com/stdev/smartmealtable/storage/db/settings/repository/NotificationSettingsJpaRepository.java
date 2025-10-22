package com.stdev.smartmealtable.storage.db.settings.repository;

import com.stdev.smartmealtable.storage.db.settings.entity.NotificationSettingsJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
    @Query("SELECT n FROM NotificationSettingsJpaEntity n WHERE n.memberId = :memberId")
    Optional<NotificationSettingsJpaEntity> findByMemberId(@Param("memberId") Long memberId);
}

package com.stdev.smartmealtable.domain.settings.repository;

import com.stdev.smartmealtable.domain.settings.entity.NotificationSettings;

import java.util.Optional;

/**
 * 알림 설정 Repository 인터페이스
 */
public interface NotificationSettingsRepository {
    
    /**
     * 알림 설정 저장
     * 
     * @param notificationSettings 알림 설정
     * @return 저장된 알림 설정
     */
    NotificationSettings save(NotificationSettings notificationSettings);
    
    /**
     * 회원 ID로 알림 설정 조회
     * 
     * @param memberId 회원 ID
     * @return 알림 설정 (Optional)
     */
    Optional<NotificationSettings> findByMemberId(Long memberId);
    
    /**
     * 알림 설정 삭제
     * 
     * @param notificationSettings 알림 설정
     */
    void delete(NotificationSettings notificationSettings);
}

package com.stdev.smartmealtable.domain.settings.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 알림 설정 도메인 엔티티 (순수 Java 객체)
 * 
 * <p>회원의 알림 설정을 관리합니다.</p>
 * <p>JPA 연관관계 매핑 없이 논리적 FK(member_id)만 사용합니다.</p>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationSettings {

    private Long notificationSettingsId;
    private Long memberId;  // 논리 FK
    private boolean pushEnabled;
    private boolean storeNoticeEnabled;
    private boolean recommendationEnabled;
    private boolean budgetAlertEnabled;
    private boolean passwordExpiryAlertEnabled;

    /**
     * 알림 설정 생성 (기본값: 모두 활성화)
     * 
     * @param memberId 회원 ID
     * @return NotificationSettings 인스턴스
     */
    public static NotificationSettings create(Long memberId) {
        NotificationSettings settings = new NotificationSettings();
        settings.memberId = memberId;
        settings.pushEnabled = true;
        settings.storeNoticeEnabled = true;
        settings.recommendationEnabled = true;
        settings.budgetAlertEnabled = true;
        settings.passwordExpiryAlertEnabled = true;
        return settings;
    }

    /**
     * 재구성 팩토리 메서드 (Storage에서 사용)
     * 
     * @param notificationSettingsId 알림 설정 ID
     * @param memberId 회원 ID
     * @param pushEnabled 전체 푸시 알림 활성화 여부
     * @param storeNoticeEnabled 가게 공지 알림 활성화 여부
     * @param recommendationEnabled 음식점 추천 알림 활성화 여부
     * @param budgetAlertEnabled 예산 알림 활성화 여부
     * @param passwordExpiryAlertEnabled 비밀번호 만료 알림 활성화 여부
     * @return NotificationSettings 인스턴스
     */
    public static NotificationSettings reconstitute(
            Long notificationSettingsId,
            Long memberId,
            boolean pushEnabled,
            boolean storeNoticeEnabled,
            boolean recommendationEnabled,
            boolean budgetAlertEnabled,
            boolean passwordExpiryAlertEnabled
    ) {
        NotificationSettings settings = new NotificationSettings();
        settings.notificationSettingsId = notificationSettingsId;
        settings.memberId = memberId;
        settings.pushEnabled = pushEnabled;
        settings.storeNoticeEnabled = storeNoticeEnabled;
        settings.recommendationEnabled = recommendationEnabled;
        settings.budgetAlertEnabled = budgetAlertEnabled;
        settings.passwordExpiryAlertEnabled = passwordExpiryAlertEnabled;
        return settings;
    }

    /**
     * 알림 설정 업데이트
     * 
     * <p>pushEnabled가 false로 설정되면 모든 하위 알림도 자동으로 비활성화됩니다.</p>
     * <p>REQ-PROFILE-302a 요구사항을 반영합니다.</p>
     * 
     * @param pushEnabled 전체 푸시 알림 활성화 여부
     * @param storeNoticeEnabled 가게 공지 알림 활성화 여부
     * @param recommendationEnabled 음식점 추천 알림 활성화 여부
     * @param budgetAlertEnabled 예산 알림 활성화 여부
     * @param passwordExpiryAlertEnabled 비밀번호 만료 알림 활성화 여부
     */
    public void updateSettings(
            boolean pushEnabled,
            boolean storeNoticeEnabled,
            boolean recommendationEnabled,
            boolean budgetAlertEnabled,
            boolean passwordExpiryAlertEnabled
    ) {
        this.pushEnabled = pushEnabled;
        
        // pushEnabled가 false면 모든 하위 알림도 비활성화
        if (!pushEnabled) {
            this.storeNoticeEnabled = false;
            this.recommendationEnabled = false;
            this.budgetAlertEnabled = false;
            this.passwordExpiryAlertEnabled = false;
        } else {
            // pushEnabled가 true면 개별 설정 적용
            this.storeNoticeEnabled = storeNoticeEnabled;
            this.recommendationEnabled = recommendationEnabled;
            this.budgetAlertEnabled = budgetAlertEnabled;
            this.passwordExpiryAlertEnabled = passwordExpiryAlertEnabled;
        }
    }
    
    /**
     * 전체 푸시 알림 활성화
     * 
     * <p>이전 상태의 하위 알림 설정은 유지됩니다.</p>
     */
    public void enablePush() {
        this.pushEnabled = true;
    }
    
    /**
     * 전체 푸시 알림 비활성화
     * 
     * <p>모든 하위 알림도 함께 비활성화됩니다.</p>
     */
    public void disablePush() {
        this.pushEnabled = false;
        this.storeNoticeEnabled = false;
        this.recommendationEnabled = false;
        this.budgetAlertEnabled = false;
        this.passwordExpiryAlertEnabled = false;
    }
}

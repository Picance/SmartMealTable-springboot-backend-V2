package com.stdev.smartmealtable.storage.db.settings.entity;

import com.stdev.smartmealtable.domain.settings.entity.NotificationSettings;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 알림 설정 JPA 엔티티
 * Domain Entity <-> JPA Entity 변환
 */
@Entity
@Table(name = "notification_settings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationSettingsJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_settings_id")
    private Long notificationSettingsId;

    @Column(name = "member_id", nullable = false, unique = true)
    private Long memberId;

    @Column(name = "push_enabled", nullable = false)
    private Boolean pushEnabled;

    @Column(name = "store_notice_enabled", nullable = false)
    private Boolean storeNoticeEnabled;

    @Column(name = "recommendation_enabled", nullable = false)
    private Boolean recommendationEnabled;

    @Column(name = "budget_alert_enabled", nullable = false)
    private Boolean budgetAlertEnabled;

    @Column(name = "password_expiry_alert_enabled", nullable = false)
    private Boolean passwordExpiryAlertEnabled;

    /**
     * Domain -> JPA Entity 변환
     */
    public static NotificationSettingsJpaEntity from(NotificationSettings settings) {
        NotificationSettingsJpaEntity entity = new NotificationSettingsJpaEntity();
        entity.notificationSettingsId = settings.getNotificationSettingsId();
        entity.memberId = settings.getMemberId();
        entity.pushEnabled = settings.isPushEnabled();
        entity.storeNoticeEnabled = settings.isStoreNoticeEnabled();
        entity.recommendationEnabled = settings.isRecommendationEnabled();
        entity.budgetAlertEnabled = settings.isBudgetAlertEnabled();
        entity.passwordExpiryAlertEnabled = settings.isPasswordExpiryAlertEnabled();
        return entity;
    }

    /**
     * JPA Entity -> Domain 변환
     */
    public NotificationSettings toDomain() {
        return NotificationSettings.reconstitute(
                this.notificationSettingsId,
                this.memberId,
                this.pushEnabled,
                this.storeNoticeEnabled,
                this.recommendationEnabled,
                this.budgetAlertEnabled,
                this.passwordExpiryAlertEnabled
        );
    }
}

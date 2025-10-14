package com.stdev.smartmealtable.storage.db.settings.entity;

import com.stdev.smartmealtable.domain.settings.entity.AppSettings;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 앱 설정 JPA 엔티티
 * Domain Entity <-> JPA Entity 변환
 */
@Entity
@Table(name = "app_settings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AppSettingsJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "app_settings_id")
    private Long appSettingsId;

    @Column(name = "member_id", nullable = false, unique = true)
    private Long memberId;

    @Column(name = "allow_tracking", nullable = false)
    private Boolean allowTracking;

    /**
     * Domain -> JPA Entity 변환
     */
    public static AppSettingsJpaEntity from(AppSettings settings) {
        AppSettingsJpaEntity entity = new AppSettingsJpaEntity();
        entity.appSettingsId = settings.getAppSettingsId();
        entity.memberId = settings.getMemberId();
        entity.allowTracking = settings.isAllowTracking();
        return entity;
    }

    /**
     * JPA Entity -> Domain 변환
     */
    public AppSettings toDomain() {
        return AppSettings.reconstitute(
                this.appSettingsId,
                this.memberId,
                this.allowTracking
        );
    }
}

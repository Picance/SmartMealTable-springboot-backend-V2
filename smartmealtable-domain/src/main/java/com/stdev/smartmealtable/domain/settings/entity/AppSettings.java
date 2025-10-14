package com.stdev.smartmealtable.domain.settings.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 앱 설정 도메인 엔티티 (순수 Java 객체)
 * 
 * <p>회원의 앱 설정(사용자 추적 허용 여부 등)을 관리합니다.</p>
 * <p>JPA 연관관계 매핑 없이 논리적 FK(member_id)만 사용합니다.</p>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AppSettings {

    private Long appSettingsId;
    private Long memberId;  // 논리 FK
    private boolean allowTracking;

    /**
     * 앱 설정 생성 (기본값: 추적 비활성화)
     * 
     * @param memberId 회원 ID
     * @return AppSettings 인스턴스
     */
    public static AppSettings create(Long memberId) {
        AppSettings settings = new AppSettings();
        settings.memberId = memberId;
        settings.allowTracking = false;  // 기본값: 추적 허용 안 함
        return settings;
    }

    /**
     * 재구성 팩토리 메서드 (Storage에서 사용)
     * 
     * @param appSettingsId 앱 설정 ID
     * @param memberId 회원 ID
     * @param allowTracking 사용자 추적 허용 여부
     * @return AppSettings 인스턴스
     */
    public static AppSettings reconstitute(
            Long appSettingsId,
            Long memberId,
            boolean allowTracking
    ) {
        AppSettings settings = new AppSettings();
        settings.appSettingsId = appSettingsId;
        settings.memberId = memberId;
        settings.allowTracking = allowTracking;
        return settings;
    }

    /**
     * 사용자 추적 허용 설정 변경
     * 
     * @param allowTracking 사용자 추적 허용 여부
     */
    public void updateTrackingSettings(boolean allowTracking) {
        this.allowTracking = allowTracking;
    }
}

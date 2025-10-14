package com.stdev.smartmealtable.api.settings.dto;

/**
 * 앱 설정 응답 DTO
 */
public record AppSettingsServiceResponse(
        String privacyPolicyUrl,
        String termsOfServiceUrl,
        String contactEmail,
        String appVersion,
        String minSupportedVersion
) {
    /**
     * 기본 앱 설정 정보 생성
     */
    public static AppSettingsServiceResponse createDefault() {
        return new AppSettingsServiceResponse(
                "https://smartmealtable.com/policies/privacy",
                "https://smartmealtable.com/policies/terms",
                "support@smartmealtable.com",
                "1.0.0",
                "1.0.0"
        );
    }
}

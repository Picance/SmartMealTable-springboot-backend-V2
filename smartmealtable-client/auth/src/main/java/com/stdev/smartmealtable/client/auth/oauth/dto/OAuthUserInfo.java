package com.stdev.smartmealtable.client.auth.oauth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * OAuth 사용자 정보 DTO
 */
@Getter
@NoArgsConstructor
public class OAuthUserInfo {
    
    private String providerId;      // 제공자의 사용자 고유 ID
    private String email;           // 이메일
    private String name;            // 이름 (선택)
    private String profileImage;    // 프로필 이미지 (선택)
    
    public OAuthUserInfo(String providerId, String email, String name, String profileImage) {
        this.providerId = providerId;
        this.email = email;
        this.name = name;
        this.profileImage = profileImage;
    }
    
    public static OAuthUserInfo of(String providerId, String email) {
        return new OAuthUserInfo(providerId, email, null, null);
    }
    
    public static OAuthUserInfo of(String providerId, String email, String name, String profileImage) {
        return new OAuthUserInfo(providerId, email, name, profileImage);
    }
}

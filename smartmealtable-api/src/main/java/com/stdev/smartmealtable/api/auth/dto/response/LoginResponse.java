package com.stdev.smartmealtable.api.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 이메일 로그인 응답 DTO (Controller Layer)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    
    private String accessToken;
    private String refreshToken;
    private Long memberId;
    private String email;
    private String name;
    private boolean isOnboardingComplete;
    
    /**
     * Service Layer DTO로부터 생성
     */
    public static LoginResponse from(com.stdev.smartmealtable.api.auth.service.dto.LoginServiceResponse serviceResponse) {
        return new LoginResponse(
            serviceResponse.getAccessToken(),
            serviceResponse.getRefreshToken(),
            serviceResponse.getMemberId(),
            serviceResponse.getEmail(),
            serviceResponse.getName(),
            serviceResponse.isOnboardingComplete()
        );
    }
}
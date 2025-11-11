package com.stdev.smartmealtable.api.auth.service.dto;

import com.stdev.smartmealtable.domain.member.entity.Member;
import com.stdev.smartmealtable.domain.member.entity.MemberAuthentication;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 로그인 서비스 응답 DTO (Application Service Layer)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginServiceResponse {
    
    private String accessToken;
    private String refreshToken;
    private Long memberId;
    private String email;
    private String name;
    private boolean isOnboardingComplete;
    
    /**
     * Domain Entity로부터 생성 (Member + MemberAuthentication + 토큰)
     */
    public static LoginServiceResponse of(Member member, MemberAuthentication authentication,
                                          String accessToken, String refreshToken,
                                          boolean isOnboardingComplete) {
        return new LoginServiceResponse(
            accessToken,
            refreshToken,
            member.getMemberId(),
            authentication.getEmail(),
            authentication.getName(),
            isOnboardingComplete
        );
    }
}

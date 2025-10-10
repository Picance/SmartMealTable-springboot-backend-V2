package com.stdev.smartmealtable.api.member.service.password;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 비밀번호 만료 상태 응답 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PasswordExpiryStatusResponse {

    private LocalDateTime passwordChangedAt;
    private LocalDateTime passwordExpiresAt;
    private Long daysRemaining;
    private Boolean isExpired;
    private Boolean isExpiringSoon;

    public static PasswordExpiryStatusResponse of(
            LocalDateTime passwordChangedAt,
            LocalDateTime passwordExpiresAt
    ) {
        PasswordExpiryStatusResponse response = new PasswordExpiryStatusResponse();
        response.passwordChangedAt = passwordChangedAt;
        response.passwordExpiresAt = passwordExpiresAt;
        
        if (passwordExpiresAt == null) {
            response.daysRemaining = null;
            response.isExpired = false;
            response.isExpiringSoon = false;
        } else {
            LocalDateTime now = LocalDateTime.now();
            response.daysRemaining = ChronoUnit.DAYS.between(now, passwordExpiresAt);
            response.isExpired = now.isAfter(passwordExpiresAt);
            response.isExpiringSoon = now.isAfter(passwordExpiresAt.minusDays(7));
        }
        
        return response;
    }
}

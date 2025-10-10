package com.stdev.smartmealtable.api.member.service.password;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 비밀번호 만료일 연장 응답 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExtendPasswordExpiryResponse {

    private LocalDateTime newExpiresAt;
    private String message;

    public static ExtendPasswordExpiryResponse of(LocalDateTime newExpiresAt) {
        ExtendPasswordExpiryResponse response = new ExtendPasswordExpiryResponse();
        response.newExpiresAt = newExpiresAt;
        response.message = "비밀번호 만료일이 90일 연장되었습니다.";
        return response;
    }
}

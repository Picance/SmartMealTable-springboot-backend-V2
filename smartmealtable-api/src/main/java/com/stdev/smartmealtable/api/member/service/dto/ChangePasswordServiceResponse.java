package com.stdev.smartmealtable.api.member.service.dto;

/**
 * 비밀번호 변경 서비스 응답 DTO
 */
public record ChangePasswordServiceResponse(
        String message
) {
    public static ChangePasswordServiceResponse success() {
        return new ChangePasswordServiceResponse("비밀번호가 변경되었습니다.");
    }
}

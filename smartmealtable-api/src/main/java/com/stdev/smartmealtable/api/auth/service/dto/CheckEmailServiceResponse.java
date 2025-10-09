package com.stdev.smartmealtable.api.auth.service.dto;

/**
 * 이메일 중복 검증 응답 DTO
 */
public record CheckEmailServiceResponse(
        boolean available,
        String message
) {
    public static CheckEmailServiceResponse ofAvailable() {
        return new CheckEmailServiceResponse(true, "사용 가능한 이메일입니다.");
    }

    public static CheckEmailServiceResponse ofDuplicate() {
        return new CheckEmailServiceResponse(false, "이미 사용 중인 이메일입니다.");
    }
}

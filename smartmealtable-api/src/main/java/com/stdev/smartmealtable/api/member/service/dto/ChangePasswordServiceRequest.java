package com.stdev.smartmealtable.api.member.service.dto;

/**
 * 비밀번호 변경 서비스 요청 DTO
 */
public record ChangePasswordServiceRequest(
        Long memberId,
        String currentPassword,
        String newPassword
) {
}

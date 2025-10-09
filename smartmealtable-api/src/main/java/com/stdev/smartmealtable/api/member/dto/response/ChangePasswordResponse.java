package com.stdev.smartmealtable.api.member.dto.response;

import com.stdev.smartmealtable.api.member.service.dto.ChangePasswordServiceResponse;

/**
 * 비밀번호 변경 응답 DTO
 */
public record ChangePasswordResponse(
        String message
) {
    public static ChangePasswordResponse from(ChangePasswordServiceResponse serviceResponse) {
        return new ChangePasswordResponse(serviceResponse.message());
    }
}

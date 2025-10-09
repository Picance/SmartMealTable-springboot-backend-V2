package com.stdev.smartmealtable.api.auth.dto.response;

import com.stdev.smartmealtable.api.auth.service.dto.CheckEmailServiceResponse;

/**
 * 이메일 중복 검증 응답 DTO
 */
public record CheckEmailResponse(
        boolean available,
        String message
) {
    public static CheckEmailResponse from(CheckEmailServiceResponse serviceResponse) {
        return new CheckEmailResponse(
                serviceResponse.available(),
                serviceResponse.message()
        );
    }
}

package com.stdev.smartmealtable.api.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 구글 로그인 요청 DTO
 * 
 * @param authorizationCode 구글 OAuth 인증 코드
 * @param redirectUri 리다이렉트 URI
 */
public record GoogleLoginServiceRequest(
        @NotBlank(message = "Authorization code는 필수입니다.")
        String authorizationCode,
        
        @NotBlank(message = "Redirect URI는 필수입니다.")
        String redirectUri
) {
}

package com.stdev.smartmealtable.api.auth.dto.request;

/**
 * 구글 로그인 요청 DTO
 * 
 * @param authorizationCode 구글 OAuth 인증 코드
 * @param redirectUri 리다이렉트 URI
 */
public record GoogleLoginServiceRequest(
        String authorizationCode,
        String redirectUri
) {
}

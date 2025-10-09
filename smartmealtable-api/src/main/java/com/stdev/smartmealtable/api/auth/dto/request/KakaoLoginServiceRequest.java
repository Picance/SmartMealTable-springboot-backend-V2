package com.stdev.smartmealtable.api.auth.dto.request;

/**
 * 카카오 로그인 요청 DTO
 * 
 * @param authorizationCode 카카오 OAuth 인증 코드
 * @param redirectUri 리다이렉트 URI
 */
public record KakaoLoginServiceRequest(
        String authorizationCode,
        String redirectUri
) {
}

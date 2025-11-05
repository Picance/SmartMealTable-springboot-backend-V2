package com.stdev.smartmealtable.api.auth.dto.response;

/**
 * 구글 로그인 응답 DTO
 * 
 * @param accessToken JWT Access Token
 * @param refreshToken JWT Refresh Token
 * @param memberId 회원 ID
 * @param email 이메일
 * @param name 이름
 * @param profileImageUrl 프로필 이미지 URL
 * @param isNewMember 신규 회원 여부
 */
public record GoogleLoginServiceResponse(
        String accessToken,
        String refreshToken,
        Long memberId,
        String email,
        String name,
        String profileImageUrl,
        boolean isNewMember
) {
    /**
     * 신규 회원 생성
     */
    public static GoogleLoginServiceResponse ofNewMember(
            String accessToken,
            String refreshToken,
            Long memberId,
            String email,
            String name,
            String profileImageUrl
    ) {
        return new GoogleLoginServiceResponse(accessToken, refreshToken, memberId, email, name, profileImageUrl, true);
    }

    /**
     * 기존 회원
     */
    public static GoogleLoginServiceResponse ofExistingMember(
            String accessToken,
            String refreshToken,
            Long memberId,
            String email,
            String name,
            String profileImageUrl
    ) {
        return new GoogleLoginServiceResponse(accessToken, refreshToken, memberId, email, name, profileImageUrl, false);
    }
}

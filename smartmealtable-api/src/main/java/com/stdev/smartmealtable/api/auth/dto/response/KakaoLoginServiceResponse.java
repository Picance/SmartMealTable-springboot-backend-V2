package com.stdev.smartmealtable.api.auth.dto.response;

/**
 * 카카오 로그인 응답 DTO
 * 
 * @param memberId 회원 ID
 * @param email 이메일
 * @param name 이름
 * @param profileImageUrl 프로필 이미지 URL
 * @param isNewMember 신규 회원 여부
 */
public record KakaoLoginServiceResponse(
        Long memberId,
        String email,
        String name,
        String profileImageUrl,
        boolean isNewMember
) {
    /**
     * 신규 회원 생성
     */
    public static KakaoLoginServiceResponse ofNewMember(
            Long memberId,
            String email,
            String name,
            String profileImageUrl
    ) {
        return new KakaoLoginServiceResponse(memberId, email, name, profileImageUrl, true);
    }

    /**
     * 기존 회원
     */
    public static KakaoLoginServiceResponse ofExistingMember(
            Long memberId,
            String email,
            String name,
            String profileImageUrl
    ) {
        return new KakaoLoginServiceResponse(memberId, email, name, profileImageUrl, false);
    }
}

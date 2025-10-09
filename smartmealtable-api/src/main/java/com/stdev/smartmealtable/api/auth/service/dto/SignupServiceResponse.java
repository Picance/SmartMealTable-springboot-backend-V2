package com.stdev.smartmealtable.api.auth.service.dto;

import com.stdev.smartmealtable.domain.member.entity.Member;
import com.stdev.smartmealtable.domain.member.entity.MemberAuthentication;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원가입 서비스 응답 DTO (Application Service Layer)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignupServiceResponse {
    
    private Long memberId;
    private String email;
    private String name;
    
    /**
     * Domain Entity로부터 생성 (Member + MemberAuthentication 조합)
     */
    public static SignupServiceResponse of(Member member, MemberAuthentication authentication) {
        return new SignupServiceResponse(
            member.getMemberId(),
            authentication.getEmail(),
            authentication.getName()
        );
    }
}

package com.stdev.smartmealtable.api.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 이메일 회원가입 응답 DTO (Controller Layer)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignupResponse {
    
    private Long memberId;
    private String email;
    private String name;
    
    /**
     * Service Layer DTO로부터 생성
     */
    public static SignupResponse from(com.stdev.smartmealtable.api.auth.service.dto.SignupServiceResponse serviceResponse) {
        return new SignupResponse(
            serviceResponse.getMemberId(),
            serviceResponse.getEmail(),
            serviceResponse.getName()
        );
    }
}

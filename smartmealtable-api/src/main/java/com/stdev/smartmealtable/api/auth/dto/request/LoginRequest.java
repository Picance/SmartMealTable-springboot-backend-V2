package com.stdev.smartmealtable.api.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 이메일 로그인 요청 DTO (Controller Layer)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    
    @NotBlank(message = "이메일은 필수 입력값입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;
    
    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    private String password;
    
    /**
     * Service Layer DTO로 변환
     */
    public com.stdev.smartmealtable.api.auth.service.dto.LoginServiceRequest toServiceRequest() {
        return new com.stdev.smartmealtable.api.auth.service.dto.LoginServiceRequest(
            email,
            password
        );
    }
}
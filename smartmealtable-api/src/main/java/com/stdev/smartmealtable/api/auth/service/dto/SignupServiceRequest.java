package com.stdev.smartmealtable.api.auth.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원가입 서비스 요청 DTO (Application Service Layer)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignupServiceRequest {
    
    private String name;
    private String email;
    private String password;
}

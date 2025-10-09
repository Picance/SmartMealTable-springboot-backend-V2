package com.stdev.smartmealtable.api.auth.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 로그인 서비스 요청 DTO (Application Service Layer)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginServiceRequest {
    
    private String email;
    private String password;
}
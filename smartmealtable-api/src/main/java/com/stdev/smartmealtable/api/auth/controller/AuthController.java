package com.stdev.smartmealtable.api.auth.controller;

import com.stdev.smartmealtable.api.auth.dto.request.SignupRequest;
import com.stdev.smartmealtable.api.auth.dto.response.SignupResponse;
import com.stdev.smartmealtable.api.auth.service.SignupService;
import com.stdev.smartmealtable.api.auth.service.dto.SignupServiceResponse;
import com.stdev.smartmealtable.core.api.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 관련 Controller
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final SignupService signupService;
    
    /**
     * 이메일 회원가입
     */
    @PostMapping("/signup/email")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        SignupServiceResponse serviceResponse = signupService.signup(request.toServiceRequest());
        SignupResponse response = SignupResponse.from(serviceResponse);
        return ApiResponse.success(response);
    }
}

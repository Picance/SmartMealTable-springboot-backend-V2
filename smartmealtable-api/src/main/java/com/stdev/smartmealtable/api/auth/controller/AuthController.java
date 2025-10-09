package com.stdev.smartmealtable.api.auth.controller;

import com.stdev.smartmealtable.api.auth.dto.request.LoginRequest;
import com.stdev.smartmealtable.api.auth.dto.request.RefreshTokenRequest;
import com.stdev.smartmealtable.api.auth.dto.request.SignupRequest;
import com.stdev.smartmealtable.api.auth.dto.response.CheckEmailResponse;
import com.stdev.smartmealtable.api.auth.dto.response.LoginResponse;
import com.stdev.smartmealtable.api.auth.dto.response.RefreshTokenResponse;
import com.stdev.smartmealtable.api.auth.dto.response.SignupResponse;
import com.stdev.smartmealtable.api.auth.service.LoginService;
import com.stdev.smartmealtable.api.auth.service.RefreshTokenService;
import com.stdev.smartmealtable.api.auth.service.SignupService;
import com.stdev.smartmealtable.api.auth.service.dto.CheckEmailServiceResponse;
import com.stdev.smartmealtable.api.auth.service.dto.LoginServiceRequest;
import com.stdev.smartmealtable.api.auth.service.request.RefreshTokenServiceRequest;
import com.stdev.smartmealtable.api.auth.service.dto.SignupServiceRequest;
import com.stdev.smartmealtable.api.auth.service.dto.LoginServiceResponse;
import com.stdev.smartmealtable.api.auth.service.response.RefreshTokenServiceResponse;
import com.stdev.smartmealtable.api.auth.service.dto.SignupServiceResponse;
import com.stdev.smartmealtable.api.config.JwtConfig;
import com.stdev.smartmealtable.core.api.response.ApiResponse;
import com.stdev.smartmealtable.core.exception.AuthenticationException;
import com.stdev.smartmealtable.core.exception.BusinessException;
import com.stdev.smartmealtable.core.error.ErrorType;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 관련 API 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SignupService signupService;
    private final LoginService loginService;
    private final RefreshTokenService refreshTokenService;
    private final JwtConfig.JwtTokenProvider jwtTokenProvider;
    
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
    
    /**
     * 이메일 로그인
     */
    @PostMapping("/login/email")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginServiceResponse serviceResponse = loginService.login(request.toServiceRequest());
        LoginResponse response = LoginResponse.from(serviceResponse);
        return ApiResponse.success(response);
    }
    
    /**
     * 토큰 재발급
     */
    @PostMapping("/refresh")
    public ApiResponse<RefreshTokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        RefreshTokenServiceResponse serviceResponse = refreshTokenService.refreshToken(
                RefreshTokenServiceRequest.from(request.refreshToken())
        );
        RefreshTokenResponse response = new RefreshTokenResponse(
                serviceResponse.accessToken(),
                serviceResponse.refreshToken()
        );
        return ApiResponse.success(response);
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        String token = extractTokenFromHeader(authorization);
        validateToken(token);
        
        // 로그아웃 성공
        return ApiResponse.success();
    }

    /**
     * 이메일 중복 검증
     */
    @GetMapping("/check-email")
    public ApiResponse<CheckEmailResponse> checkEmail(@RequestParam String email) {
        // 이메일 형식 검증
        if (!isValidEmail(email)) {
            throw new BusinessException(ErrorType.INVALID_EMAIL_FORMAT);
        }

        CheckEmailServiceResponse serviceResponse = signupService.checkEmail(email);
        CheckEmailResponse response = CheckEmailResponse.from(serviceResponse);
        return ApiResponse.success(response);
    }
    
    private String extractTokenFromHeader(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new AuthenticationException(ErrorType.INVALID_TOKEN);
        }
        return authorization.substring(7);
    }
    
    private void validateToken(String token) {
        if (token.trim().isEmpty() || !jwtTokenProvider.isTokenValid(token)) {
            throw new AuthenticationException(ErrorType.INVALID_TOKEN);
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email != null && email.matches(emailRegex);
    }
}

package com.stdev.smartmealtable.api.auth.controller;

import com.stdev.smartmealtable.api.auth.dto.request.GoogleLoginServiceRequest;
import com.stdev.smartmealtable.api.auth.dto.request.KakaoLoginServiceRequest;
import com.stdev.smartmealtable.api.auth.dto.response.GoogleLoginServiceResponse;
import com.stdev.smartmealtable.api.auth.dto.response.KakaoLoginServiceResponse;
import com.stdev.smartmealtable.api.auth.service.GoogleLoginService;
import com.stdev.smartmealtable.api.auth.service.KakaoLoginService;
import com.stdev.smartmealtable.core.api.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 소셜 로그인 API Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class SocialLoginController {

    private final KakaoLoginService kakaoLoginService;
    private final GoogleLoginService googleLoginService;

    /**
     * 카카오 로그인
     * 
     * @param request 카카오 인가 코드 및 리다이렉트 URI
     * @return 로그인 결과 (회원 정보, 신규 회원 여부)
     */
    @PostMapping("/login/kakao")
    public ResponseEntity<ApiResponse<KakaoLoginServiceResponse>> kakaoLogin(
            @Valid @RequestBody KakaoLoginServiceRequest request
    ) {
        log.info("카카오 로그인 요청: redirectUri={}", request.redirectUri());
        
        KakaoLoginServiceResponse response = kakaoLoginService.login(request);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 구글 로그인
     * 
     * @param request 구글 인가 코드 및 리다이렉트 URI
     * @return 로그인 결과 (회원 정보, 신규 회원 여부)
     */
    @PostMapping("/login/google")
    public ResponseEntity<ApiResponse<GoogleLoginServiceResponse>> googleLogin(
            @Valid @RequestBody GoogleLoginServiceRequest request
    ) {
        log.info("구글 로그인 요청: redirectUri={}", request.redirectUri());
        
        GoogleLoginServiceResponse response = googleLoginService.login(request);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

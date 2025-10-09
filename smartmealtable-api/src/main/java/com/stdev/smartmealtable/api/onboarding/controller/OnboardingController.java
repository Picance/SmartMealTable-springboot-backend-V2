package com.stdev.smartmealtable.api.onboarding.controller;

import com.stdev.smartmealtable.api.onboarding.dto.OnboardingProfileRequest;
import com.stdev.smartmealtable.api.onboarding.dto.OnboardingProfileResponse;
import com.stdev.smartmealtable.api.onboarding.service.OnboardingProfileService;
import com.stdev.smartmealtable.api.onboarding.service.dto.OnboardingProfileServiceRequest;
import com.stdev.smartmealtable.api.onboarding.service.dto.OnboardingProfileServiceResponse;
import com.stdev.smartmealtable.core.api.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 온보딩 API Controller
 * 신규 가입 회원의 초기 정보 설정
 */
@RestController
@RequestMapping("/api/v1/onboarding")
@RequiredArgsConstructor
@Slf4j
public class OnboardingController {

    private final OnboardingProfileService onboardingProfileService;

    /**
     * 온보딩 - 프로필 설정 (닉네임 및 소속 그룹)
     * POST /api/v1/onboarding/profile
     */
    @PostMapping("/profile")
    public ResponseEntity<ApiResponse<OnboardingProfileResponse>> updateProfile(
            @Valid @RequestBody OnboardingProfileRequest request,
            @RequestHeader(value = "X-Member-Id", required = false) Long memberId
            // TODO: JWT 인증 구현 후 @AuthenticationPrincipal로 memberId 추출
    ) {
        log.info("온보딩 프로필 설정 API 호출 - nickname: {}, groupId: {}", 
                request.nickname(), request.groupId());

        // TODO: JWT에서 memberId 추출 (임시로 헤더에서 받거나 1L 사용)
        Long authenticatedMemberId = (memberId != null) ? memberId : 1L;

        OnboardingProfileServiceRequest serviceRequest = new OnboardingProfileServiceRequest(
                authenticatedMemberId,
                request.nickname(),
                request.groupId()
        );

        OnboardingProfileServiceResponse serviceResponse = onboardingProfileService.updateProfile(serviceRequest);

        OnboardingProfileResponse response = new OnboardingProfileResponse(
                serviceResponse.memberId(),
                serviceResponse.nickname(),
                new OnboardingProfileResponse.GroupInfo(
                        serviceResponse.group().groupId(),
                        serviceResponse.group().name(),
                        serviceResponse.group().type(),
                        serviceResponse.group().address()
                )
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

package com.stdev.smartmealtable.api.onboarding.controller;

import com.stdev.smartmealtable.api.onboarding.dto.request.OnboardingAddressRequest;
import com.stdev.smartmealtable.api.onboarding.dto.request.OnboardingProfileRequest;
import com.stdev.smartmealtable.api.onboarding.dto.response.OnboardingAddressResponse;
import com.stdev.smartmealtable.api.onboarding.dto.response.OnboardingProfileResponse;
import com.stdev.smartmealtable.api.onboarding.service.OnboardingAddressService;
import com.stdev.smartmealtable.api.onboarding.service.OnboardingProfileService;
import com.stdev.smartmealtable.api.onboarding.service.dto.OnboardingAddressServiceRequest;
import com.stdev.smartmealtable.api.onboarding.service.dto.OnboardingAddressServiceResponse;
import com.stdev.smartmealtable.api.onboarding.service.dto.OnboardingProfileServiceRequest;
import com.stdev.smartmealtable.api.onboarding.service.dto.OnboardingProfileServiceResponse;
import com.stdev.smartmealtable.core.api.response.ApiResponse;
import com.stdev.smartmealtable.core.auth.AuthUser;
import com.stdev.smartmealtable.core.auth.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
    private final OnboardingAddressService onboardingAddressService;

    /**
     * 온보딩 - 프로필 설정 (닉네임 및 소속 그룹)
     * POST /api/v1/onboarding/profile
     */
    @PostMapping("/profile")
    public ResponseEntity<ApiResponse<OnboardingProfileResponse>> updateProfile(
            @Valid @RequestBody OnboardingProfileRequest request,
            @AuthUser AuthenticatedUser authenticatedUser
    ) {
        log.info("온보딩 프로필 설정 API 호출 - memberId: {}, nickname: {}, groupId: {}", 
                authenticatedUser.memberId(), request.nickname(), request.groupId());

        OnboardingProfileServiceRequest serviceRequest = new OnboardingProfileServiceRequest(
                authenticatedUser.memberId(),
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
                        serviceResponse.group().type().name(),
                        serviceResponse.group().address()
                )
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * 온보딩 - 주소 등록
     * POST /api/v1/onboarding/address
     */
    @PostMapping("/address")
    public ResponseEntity<ApiResponse<OnboardingAddressResponse>> registerAddress(
            @Valid @RequestBody OnboardingAddressRequest request,
            @AuthUser AuthenticatedUser authenticatedUser
    ) {
        log.info("온보딩 주소 등록 API 호출 - memberId: {}, alias: {}, isPrimary: {}", 
                authenticatedUser.memberId(), request.alias(), request.isPrimary());

        OnboardingAddressServiceRequest serviceRequest = OnboardingAddressServiceRequest.of(
                authenticatedUser.memberId(),
                request.alias(),
                request.lotNumberAddress(),
                request.streetNameAddress(),
                request.detailedAddress(),
                request.latitude(),
                request.longitude(),
                request.addressType(),
                request.isPrimary()
        );

        OnboardingAddressServiceResponse serviceResponse = onboardingAddressService.registerAddress(serviceRequest);

        OnboardingAddressResponse response = new OnboardingAddressResponse(
                serviceResponse.addressHistoryId(),
                serviceResponse.alias(),
                serviceResponse.lotNumberAddress(),
                serviceResponse.streetNameAddress(),
                serviceResponse.detailedAddress(),
                serviceResponse.latitude(),
                serviceResponse.longitude(),
                serviceResponse.addressType(),
                serviceResponse.isPrimary()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }
}

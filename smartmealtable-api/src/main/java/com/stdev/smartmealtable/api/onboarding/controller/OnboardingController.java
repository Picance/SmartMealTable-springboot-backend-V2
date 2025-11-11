package com.stdev.smartmealtable.api.onboarding.controller;

import com.stdev.smartmealtable.api.onboarding.controller.dto.GetFoodsResponse;
import com.stdev.smartmealtable.api.onboarding.controller.dto.SaveFoodPreferencesRequest;
import com.stdev.smartmealtable.api.onboarding.controller.dto.SaveFoodPreferencesResponse;
import com.stdev.smartmealtable.api.onboarding.controller.dto.SetBudgetRequest;
import com.stdev.smartmealtable.api.onboarding.controller.dto.SetBudgetResponse;
import com.stdev.smartmealtable.api.onboarding.controller.dto.SetPreferencesRequest;
import com.stdev.smartmealtable.api.onboarding.controller.dto.SetPreferencesResponse;
import com.stdev.smartmealtable.api.onboarding.dto.request.OnboardingAddressRequest;
import com.stdev.smartmealtable.api.onboarding.dto.request.OnboardingProfileRequest;
import com.stdev.smartmealtable.api.onboarding.dto.request.PolicyAgreementRequest;
import com.stdev.smartmealtable.api.onboarding.dto.response.OnboardingAddressResponse;
import com.stdev.smartmealtable.api.onboarding.dto.response.OnboardingProfileResponse;
import com.stdev.smartmealtable.api.onboarding.dto.response.PolicyAgreementResponse;
import com.stdev.smartmealtable.api.onboarding.service.GetFoodsService;
import com.stdev.smartmealtable.api.onboarding.service.OnboardingAddressService;
import com.stdev.smartmealtable.api.onboarding.service.OnboardingProfileService;
import com.stdev.smartmealtable.api.onboarding.service.PolicyAgreementService;
import com.stdev.smartmealtable.api.onboarding.service.SaveFoodPreferencesService;
import com.stdev.smartmealtable.api.onboarding.service.SetBudgetService;
import com.stdev.smartmealtable.api.onboarding.service.SetPreferencesService;
import com.stdev.smartmealtable.api.onboarding.service.dto.GetFoodsServiceResponse;
import com.stdev.smartmealtable.api.onboarding.service.dto.OnboardingAddressServiceRequest;
import com.stdev.smartmealtable.api.onboarding.service.dto.OnboardingAddressServiceResponse;
import com.stdev.smartmealtable.api.onboarding.service.dto.OnboardingProfileServiceRequest;
import com.stdev.smartmealtable.api.onboarding.service.dto.OnboardingProfileServiceResponse;
import com.stdev.smartmealtable.api.onboarding.service.dto.SaveFoodPreferencesServiceRequest;
import com.stdev.smartmealtable.api.onboarding.service.dto.SaveFoodPreferencesServiceResponse;
import com.stdev.smartmealtable.api.onboarding.service.dto.SetBudgetServiceResponse;
import com.stdev.smartmealtable.api.onboarding.service.request.PolicyAgreementServiceRequest;
import com.stdev.smartmealtable.api.onboarding.service.response.PolicyAgreementServiceResponse;
import com.stdev.smartmealtable.core.api.response.ApiResponse;
import com.stdev.smartmealtable.core.auth.AuthUser;
import com.stdev.smartmealtable.core.auth.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    private final SetBudgetService setBudgetService;
    private final SetPreferencesService setPreferencesService;
    private final GetFoodsService getFoodsService;
    private final SaveFoodPreferencesService saveFoodPreferencesService;
    private final PolicyAgreementService policyAgreementService;

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
        log.info("온보딩 주소 등록 API 호출 - memberId: {}, alias: {}", 
                authenticatedUser.memberId(), request.alias());

        // String addressType을 AddressType ENUM으로 변환
        com.stdev.smartmealtable.domain.common.vo.AddressType addressTypeEnum = null;
        if (request.addressType() != null && !request.addressType().isBlank()) {
            try {
                addressTypeEnum = com.stdev.smartmealtable.domain.common.vo.AddressType.valueOf(request.addressType().toUpperCase());
            } catch (IllegalArgumentException e) {
                // 유효하지 않은 주소 유형이면 null로 설정
                addressTypeEnum = null;
            }
        }

        OnboardingAddressServiceRequest serviceRequest = OnboardingAddressServiceRequest.of(
                authenticatedUser.memberId(),
                request.alias(),
                request.lotNumberAddress(),
                request.streetNameAddress(),
                request.detailedAddress(),
                request.latitude(),
                request.longitude(),
                addressTypeEnum
        );

        OnboardingAddressServiceResponse serviceResponse = onboardingAddressService.registerAddress(serviceRequest);

        String responseAddressType = serviceResponse.addressType() != null
                ? serviceResponse.addressType().name()
                : null;

        OnboardingAddressResponse response = new OnboardingAddressResponse(
                serviceResponse.addressHistoryId(),
                serviceResponse.alias(),
                serviceResponse.lotNumberAddress(),
                serviceResponse.streetNameAddress(),
                serviceResponse.detailedAddress(),
                serviceResponse.latitude(),
                serviceResponse.longitude(),
                responseAddressType,
                serviceResponse.isPrimary()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }
    
    /**
     * 온보딩 - 예산 설정
     * POST /api/v1/onboarding/budget
     */
    @PostMapping("/budget")
    public ResponseEntity<ApiResponse<SetBudgetResponse>> setBudget(
            @Valid @RequestBody SetBudgetRequest request,
            @AuthUser AuthenticatedUser authenticatedUser
    ) {
        log.info("온보딩 예산 설정 API 호출 - memberId: {}, monthlyBudget: {}, dailyBudget: {}", 
                authenticatedUser.memberId(), request.getMonthlyBudget(), request.getDailyBudget());

        SetBudgetServiceResponse serviceResponse = setBudgetService.setBudget(
                authenticatedUser.memberId(),
                request.toServiceRequest()
        );

        SetBudgetResponse response = SetBudgetResponse.from(serviceResponse);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }
    
    /**
     * 온보딩 - 취향 설정 (추천 유형 + 카테고리별 선호도)
     * POST /api/v1/onboarding/preferences
     */
    @PostMapping("/preferences")
    public ResponseEntity<ApiResponse<SetPreferencesResponse>> setPreferences(
            @Valid @RequestBody SetPreferencesRequest request,
            @AuthUser AuthenticatedUser authenticatedUser
    ) {
        log.info("온보딩 취향 설정 API 호출 - memberId: {}, recommendationType: {}, preferences count: {}", 
                authenticatedUser.memberId(), request.getRecommendationType(), request.getPreferences().size());

        var serviceRequest = new com.stdev.smartmealtable.api.onboarding.service.dto.SetPreferencesServiceRequest(
                request.getRecommendationType(),
                request.getPreferences().stream()
                        .map(p -> new com.stdev.smartmealtable.api.onboarding.service.dto.SetPreferencesServiceRequest.PreferenceItem(
                                p.getCategoryId(),
                                p.getWeight()
                        ))
                        .toList()
        );

        var serviceResponse = setPreferencesService.setPreferences(authenticatedUser.memberId(), serviceRequest);

        SetPreferencesResponse response = SetPreferencesResponse.from(serviceResponse);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * 온보딩 - 음식 목록 조회 (이미지 그리드용)
     * GET /api/v1/onboarding/foods
     */
    @GetMapping("/foods")
    public ResponseEntity<ApiResponse<GetFoodsResponse>> getFoods(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("온보딩 음식 목록 조회 API 호출 - categoryId: {}, page: {}, size: {}", categoryId, page, size);

        GetFoodsServiceResponse serviceResponse = getFoodsService.getFoods(categoryId, page, size);
        GetFoodsResponse response = GetFoodsResponse.from(serviceResponse);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 온보딩 - 개별 음식 선호도 저장
     * POST /api/v1/onboarding/food-preferences
     */
    @PostMapping("/food-preferences")
    public ResponseEntity<ApiResponse<SaveFoodPreferencesResponse>> saveFoodPreferences(
            @Valid @RequestBody SaveFoodPreferencesRequest request,
            @AuthUser AuthenticatedUser authenticatedUser
    ) {
        log.info("개별 음식 선호도 저장 API 호출 - memberId: {}, foodCount: {}",
                authenticatedUser.memberId(), request.preferredFoodIds().size());

        SaveFoodPreferencesServiceRequest serviceRequest = new SaveFoodPreferencesServiceRequest(
                request.preferredFoodIds()
        );

        SaveFoodPreferencesServiceResponse serviceResponse = saveFoodPreferencesService.saveFoodPreferences(
                authenticatedUser.memberId(),
                serviceRequest
        );

        SaveFoodPreferencesResponse response = SaveFoodPreferencesResponse.from(serviceResponse);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * 온보딩 - 약관 동의
     * POST /api/v1/onboarding/policy-agreements
     */
    @PostMapping("/policy-agreements")
    public ResponseEntity<ApiResponse<PolicyAgreementResponse>> agreeToPolicies(
            @Valid @RequestBody PolicyAgreementRequest request,
            @AuthUser AuthenticatedUser authenticatedUser
    ) {
        log.info("온보딩 약관 동의 API 호출 - memberId: {}, agreements count: {}",
                authenticatedUser.memberId(), request.getAgreements().size());

        List<PolicyAgreementServiceRequest> serviceRequests = request.getAgreements().stream()
                .map(item -> new PolicyAgreementServiceRequest(item.getPolicyId(), item.getIsAgreed()))
                .toList();

        PolicyAgreementServiceResponse serviceResponse = policyAgreementService.agreeToPolicies(
                authenticatedUser.memberId(),
                serviceRequests
        );

        PolicyAgreementResponse response = PolicyAgreementResponse.of(
                serviceResponse.getMemberAuthenticationId(),
                serviceResponse.getAgreedCount()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }
}

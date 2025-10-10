package com.stdev.smartmealtable.api.member.controller;

import com.stdev.smartmealtable.api.member.controller.preference.AddFoodPreferenceRequest;
import com.stdev.smartmealtable.api.member.controller.preference.AddFoodPreferenceResponse;
import com.stdev.smartmealtable.api.member.controller.preference.UpdateCategoryPreferencesRequest;
import com.stdev.smartmealtable.api.member.controller.preference.UpdateCategoryPreferencesResponse;
import com.stdev.smartmealtable.api.member.controller.preference.UpdateFoodPreferenceRequest;
import com.stdev.smartmealtable.api.member.controller.preference.UpdateFoodPreferenceResponse;
import com.stdev.smartmealtable.api.member.service.preference.*;
import com.stdev.smartmealtable.core.api.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 회원 선호도 관리 API 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/members/me/preferences")
@RequiredArgsConstructor
public class PreferenceController {

    private final GetPreferencesService getPreferencesService;
    private final UpdateCategoryPreferencesService updateCategoryPreferencesService;
    private final AddFoodPreferenceService addFoodPreferenceService;
    private final UpdateFoodPreferenceService updateFoodPreferenceService;
    private final DeleteFoodPreferenceService deleteFoodPreferenceService;

    /**
     * 선호도 조회 (카테고리 선호도 + 음식 선호도)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<GetPreferencesServiceResponse>> getPreferences(
            @RequestHeader("X-Member-Id") Long memberId  // TODO: JWT 인증으로 대체
    ) {
        GetPreferencesServiceResponse response = getPreferencesService.execute(memberId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 카테고리 선호도 수정
     */
    @PutMapping("/categories")
    public ResponseEntity<ApiResponse<UpdateCategoryPreferencesResponse>> updateCategoryPreferences(
            @RequestHeader("X-Member-Id") Long memberId,  // TODO: JWT 인증으로 대체
            @Valid @RequestBody UpdateCategoryPreferencesRequest request
    ) {
        UpdateCategoryPreferencesServiceResponse serviceResponse = 
                updateCategoryPreferencesService.execute(memberId, request.toServiceRequest());
        UpdateCategoryPreferencesResponse response = UpdateCategoryPreferencesResponse.from(serviceResponse);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 음식 선호도 추가
     */
    @PostMapping("/foods")
    public ResponseEntity<ApiResponse<AddFoodPreferenceResponse>> addFoodPreference(
            @RequestHeader("X-Member-Id") Long memberId,  // TODO: JWT 인증으로 대체
            @Valid @RequestBody AddFoodPreferenceRequest request
    ) {
        AddFoodPreferenceServiceResponse serviceResponse = 
                addFoodPreferenceService.execute(memberId, request.toServiceRequest());
        AddFoodPreferenceResponse response = AddFoodPreferenceResponse.from(serviceResponse);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * 음식 선호도 변경
     */
    @PutMapping("/foods/{foodPreferenceId}")
    public ResponseEntity<ApiResponse<UpdateFoodPreferenceResponse>> updateFoodPreference(
            @RequestHeader("X-Member-Id") Long memberId,  // TODO: JWT 인증으로 대체
            @PathVariable Long foodPreferenceId,
            @Valid @RequestBody UpdateFoodPreferenceRequest request
    ) {
        UpdateFoodPreferenceServiceResponse serviceResponse = 
                updateFoodPreferenceService.execute(memberId, foodPreferenceId, request.toServiceRequest());
        UpdateFoodPreferenceResponse response = UpdateFoodPreferenceResponse.from(serviceResponse);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 음식 선호도 삭제
     */
    @DeleteMapping("/foods/{foodPreferenceId}")
    public ResponseEntity<Void> deleteFoodPreference(
            @RequestHeader("X-Member-Id") Long memberId,  // TODO: JWT 인증으로 대체
            @PathVariable Long foodPreferenceId
    ) {
        deleteFoodPreferenceService.execute(memberId, foodPreferenceId);
        return ResponseEntity.noContent().build();
    }
}

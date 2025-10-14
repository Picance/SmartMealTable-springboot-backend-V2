package com.stdev.smartmealtable.api.settings.controller;

import com.stdev.smartmealtable.core.auth.AuthenticatedUser;
import com.stdev.smartmealtable.api.settings.dto.AppSettingsServiceResponse;
import com.stdev.smartmealtable.api.settings.dto.TrackingSettingsServiceResponse;
import com.stdev.smartmealtable.api.settings.dto.UpdateTrackingSettingsServiceRequest;
import com.stdev.smartmealtable.api.settings.service.AppSettingsApplicationService;
import com.stdev.smartmealtable.core.api.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 앱 설정 Controller
 */
@RestController
@RequestMapping("/api/v1/settings/app")
@RequiredArgsConstructor
@Slf4j
public class AppSettingsController {

    private final AppSettingsApplicationService appSettingsApplicationService;

    /**
     * 앱 설정 정보 조회
     * 
     * @return 앱 설정 정보
     */
    @GetMapping
    public ApiResponse<AppSettingsServiceResponse> getAppSettings() {
        log.info("GET /api/v1/settings/app");
        
        AppSettingsServiceResponse response = appSettingsApplicationService.getAppSettings();
        
        return ApiResponse.success(response);
    }

    /**
     * 사용자 추적 설정 변경
     * 
     * @param user 인증된 사용자
     * @param request 변경 요청
     * @return 변경된 추적 설정
     */
    @PutMapping("/tracking")
    public ApiResponse<TrackingSettingsServiceResponse> updateTrackingSettings(
            AuthenticatedUser user,
            @RequestBody @Valid UpdateTrackingSettingsServiceRequest request
    ) {
        log.info("PUT /api/v1/settings/app/tracking - memberId: {}", user.memberId());
        
        TrackingSettingsServiceResponse response = 
                appSettingsApplicationService.updateTrackingSettings(user.memberId(), request);
        
        return ApiResponse.success(response);
    }
}

package com.stdev.smartmealtable.api.settings.controller;

import com.stdev.smartmealtable.core.auth.AuthenticatedUser;
import com.stdev.smartmealtable.api.settings.dto.NotificationSettingsServiceResponse;
import com.stdev.smartmealtable.api.settings.dto.UpdateNotificationSettingsServiceRequest;
import com.stdev.smartmealtable.api.settings.service.NotificationSettingsApplicationService;
import com.stdev.smartmealtable.core.api.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 알림 설정 Controller
 */
@RestController
@RequestMapping("/api/v1/members/me/notification-settings")
@RequiredArgsConstructor
@Slf4j
public class NotificationSettingsController {

    private final NotificationSettingsApplicationService notificationSettingsApplicationService;

    /**
     * 알림 설정 조회
     * 
     * @param user 인증된 사용자
     * @return 알림 설정
     */
    @GetMapping
    public ApiResponse<NotificationSettingsServiceResponse> getNotificationSettings(
            AuthenticatedUser user
    ) {
        log.info("GET /api/v1/members/me/notification-settings - memberId: {}", user.memberId());
        
        NotificationSettingsServiceResponse response = 
                notificationSettingsApplicationService.getNotificationSettings(user.memberId());
        
        return ApiResponse.success(response);
    }

    /**
     * 알림 설정 변경
     * 
     * @param user 인증된 사용자
     * @param request 변경 요청
     * @return 변경된 알림 설정
     */
    @PutMapping
    public ApiResponse<NotificationSettingsServiceResponse> updateNotificationSettings(
            AuthenticatedUser user,
            @RequestBody @Valid UpdateNotificationSettingsServiceRequest request
    ) {
        log.info("PUT /api/v1/members/me/notification-settings - memberId: {}", user.memberId());
        
        NotificationSettingsServiceResponse response = 
                notificationSettingsApplicationService.updateNotificationSettings(user.memberId(), request);
        
        return ApiResponse.success(response);
    }
}

package com.stdev.smartmealtable.api.member.controller;

import com.stdev.smartmealtable.api.member.service.password.ExtendPasswordExpiryService;
import com.stdev.smartmealtable.api.member.service.password.ExtendPasswordExpiryResponse;
import com.stdev.smartmealtable.api.member.service.password.GetPasswordExpiryStatusService;
import com.stdev.smartmealtable.api.member.service.password.PasswordExpiryStatusResponse;
import com.stdev.smartmealtable.core.api.response.ApiResponse;
import com.stdev.smartmealtable.core.auth.AuthUser;
import com.stdev.smartmealtable.core.auth.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 비밀번호 만료 관리 API Controller
 */
@RestController
@RequestMapping("/api/v1/members/me/password")
@RequiredArgsConstructor
public class PasswordExpiryController {

    private final GetPasswordExpiryStatusService getPasswordExpiryStatusService;
    private final ExtendPasswordExpiryService extendPasswordExpiryService;

    /**
     * 3.11.1 비밀번호 만료 상태 조회
     */
    @GetMapping("/expiry-status")
    public ResponseEntity<ApiResponse<PasswordExpiryStatusResponse>> getPasswordExpiryStatus(
            @AuthUser AuthenticatedUser authenticatedUser
    ) {
        PasswordExpiryStatusResponse response = getPasswordExpiryStatusService
                .getPasswordExpiryStatus(authenticatedUser.memberId());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 3.11.2 비밀번호 만료일 연장
     */
    @PostMapping("/extend-expiry")
    public ResponseEntity<ApiResponse<ExtendPasswordExpiryResponse>> extendPasswordExpiry(
            @AuthUser AuthenticatedUser authenticatedUser
    ) {
        ExtendPasswordExpiryResponse response = extendPasswordExpiryService
                .extendPasswordExpiry(authenticatedUser.memberId());

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

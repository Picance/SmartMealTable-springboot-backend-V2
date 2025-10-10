package com.stdev.smartmealtable.api.member.controller;

import com.stdev.smartmealtable.api.member.service.social.AddSocialAccountService;
import com.stdev.smartmealtable.api.member.service.social.AddSocialAccountServiceRequest;
import com.stdev.smartmealtable.api.member.service.social.AddSocialAccountServiceResponse;
import com.stdev.smartmealtable.api.member.service.social.GetSocialAccountListService;
import com.stdev.smartmealtable.api.member.service.social.RemoveSocialAccountService;
import com.stdev.smartmealtable.api.member.service.social.SocialAccountListServiceResponse;
import com.stdev.smartmealtable.core.api.response.ApiResponse;
import com.stdev.smartmealtable.core.auth.AuthUser;
import com.stdev.smartmealtable.core.auth.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 소셜 계정 연동 관리 API Controller
 */
@RestController
@RequestMapping("/api/v1/members/me/social-accounts")
@RequiredArgsConstructor
public class SocialAccountController {

    private final GetSocialAccountListService getSocialAccountListService;
    private final AddSocialAccountService addSocialAccountService;
    private final RemoveSocialAccountService removeSocialAccountService;

    /**
     * 3.10.1 연동된 소셜 계정 목록 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<SocialAccountListServiceResponse>> getSocialAccountList(
            @AuthUser AuthenticatedUser authenticatedUser
    ) {
        SocialAccountListServiceResponse response = getSocialAccountListService
                .getSocialAccountList(authenticatedUser.memberId());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 3.10.2 소셜 계정 추가 연동
     */
    @PostMapping
    public ResponseEntity<ApiResponse<AddSocialAccountServiceResponse>> addSocialAccount(
            @AuthUser AuthenticatedUser authenticatedUser,
            @Valid @RequestBody AddSocialAccountServiceRequest request
    ) {
        AddSocialAccountServiceResponse response = addSocialAccountService
                .addSocialAccount(authenticatedUser.memberId(), request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * 3.10.3 소셜 계정 연동 해제
     */
    @DeleteMapping("/{socialAccountId}")
    public ResponseEntity<Void> removeSocialAccount(
            @AuthUser AuthenticatedUser authenticatedUser,
            @PathVariable Long socialAccountId
    ) {
        removeSocialAccountService.removeSocialAccount(authenticatedUser.memberId(), socialAccountId);

        return ResponseEntity.noContent().build();
    }
}

package com.stdev.smartmealtable.api.member.controller;

import com.stdev.smartmealtable.api.member.dto.request.ChangePasswordRequest;
import com.stdev.smartmealtable.api.member.dto.request.UpdateProfileRequest;
import com.stdev.smartmealtable.api.member.dto.request.WithdrawMemberRequest;
import com.stdev.smartmealtable.api.member.dto.response.ChangePasswordResponse;
import com.stdev.smartmealtable.api.member.dto.response.MemberProfileResponse;
import com.stdev.smartmealtable.api.member.dto.response.UpdateProfileResponse;
import com.stdev.smartmealtable.api.member.service.ChangePasswordService;
import com.stdev.smartmealtable.api.member.service.MemberProfileService;
import com.stdev.smartmealtable.api.member.service.WithdrawMemberService;
import com.stdev.smartmealtable.api.member.service.dto.ChangePasswordServiceResponse;
import com.stdev.smartmealtable.api.member.service.dto.UpdateProfileServiceRequest;
import com.stdev.smartmealtable.core.api.response.ApiResponse;
import com.stdev.smartmealtable.core.auth.AuthUser;
import com.stdev.smartmealtable.core.auth.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * 회원 관리 API 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final ChangePasswordService changePasswordService;
    private final WithdrawMemberService withdrawMemberService;
    private final MemberProfileService memberProfileService;

    /**
     * 10.1 내 프로필 조회
     */
    @GetMapping("/me")
    public ApiResponse<MemberProfileResponse> getMyProfile(@AuthUser AuthenticatedUser user) {
        MemberProfileResponse response = memberProfileService.getProfile(user.memberId());
        return ApiResponse.success(response);
    }

    /**
     * 10.2 프로필 수정 (닉네임, 그룹)
     */
    @PutMapping("/me")
    public ApiResponse<UpdateProfileResponse> updateProfile(
            @AuthUser AuthenticatedUser user,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        UpdateProfileServiceRequest serviceRequest = UpdateProfileServiceRequest.of(
                user.memberId(),
                request.getNickname(),
                request.getGroupId()
        );
        UpdateProfileResponse response = memberProfileService.updateProfile(serviceRequest);
        return ApiResponse.success(response);
    }

    /**
     * 비밀번호 변경
     */
    @PutMapping("/me/password")
    public ApiResponse<ChangePasswordResponse> changePassword(
            @AuthUser AuthenticatedUser user,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        ChangePasswordServiceResponse serviceResponse = changePasswordService.changePassword(
                request.toServiceRequest(user.memberId())
        );
        ChangePasswordResponse response = ChangePasswordResponse.from(serviceResponse);
        return ApiResponse.success(response);
    }

    /**
     * 회원 탈퇴
     */
    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void withdrawMember(
            @AuthUser AuthenticatedUser user,
            @Valid @RequestBody WithdrawMemberRequest request
    ) {
        withdrawMemberService.withdrawMember(request.toServiceRequest(user.memberId()));
    }
}

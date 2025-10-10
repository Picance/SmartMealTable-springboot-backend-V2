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
     * TODO: @RequestHeader를 JWT ArgumentResolver로 대체 필요
     */
    @GetMapping("/me")
    public ApiResponse<MemberProfileResponse> getMyProfile(@RequestHeader("X-Member-Id") Long memberId) {
        MemberProfileResponse response = memberProfileService.getProfile(memberId);
        return ApiResponse.success(response);
    }

    /**
     * 10.2 프로필 수정 (닉네임, 그룹)
     * TODO: @RequestHeader를 JWT ArgumentResolver로 대체 필요
     */
    @PutMapping("/me")
    public ApiResponse<UpdateProfileResponse> updateProfile(
            @RequestHeader("X-Member-Id") Long memberId,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        UpdateProfileServiceRequest serviceRequest = UpdateProfileServiceRequest.of(
                memberId,
                request.getNickname(),
                request.getGroupId()
        );
        UpdateProfileResponse response = memberProfileService.updateProfile(serviceRequest);
        return ApiResponse.success(response);
    }

    /**
     * 비밀번호 변경
     * TODO: @RequestHeader를 JWT ArgumentResolver로 대체 필요
     */
    @PutMapping("/me/password")
    public ApiResponse<ChangePasswordResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @RequestHeader("X-Member-Id") Long memberId
    ) {
        ChangePasswordServiceResponse serviceResponse = changePasswordService.changePassword(
                request.toServiceRequest(memberId)
        );
        ChangePasswordResponse response = ChangePasswordResponse.from(serviceResponse);
        return ApiResponse.success(response);
    }

    /**
     * 회원 탈퇴
     * TODO: @RequestHeader를 JWT ArgumentResolver로 대체 필요
     */
    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void withdrawMember(
            @Valid @RequestBody WithdrawMemberRequest request,
            @RequestHeader("X-Member-Id") Long memberId
    ) {
        withdrawMemberService.withdrawMember(request.toServiceRequest(memberId));
    }
}

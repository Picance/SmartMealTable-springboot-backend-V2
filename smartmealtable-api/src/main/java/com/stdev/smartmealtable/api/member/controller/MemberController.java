package com.stdev.smartmealtable.api.member.controller;

import com.stdev.smartmealtable.api.member.dto.request.ChangePasswordRequest;
import com.stdev.smartmealtable.api.member.dto.request.WithdrawMemberRequest;
import com.stdev.smartmealtable.api.member.dto.response.ChangePasswordResponse;
import com.stdev.smartmealtable.api.member.service.ChangePasswordService;
import com.stdev.smartmealtable.api.member.service.WithdrawMemberService;
import com.stdev.smartmealtable.api.member.service.dto.ChangePasswordServiceResponse;
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

    /**
     * 비밀번호 변경
     */
    @PutMapping("/me/password")
    public ApiResponse<ChangePasswordResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @RequestHeader("X-Member-Id") Long memberId  // TODO: JWT에서 추출하도록 변경
    ) {
        ChangePasswordServiceResponse serviceResponse = changePasswordService.changePassword(
                request.toServiceRequest(memberId)
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
            @Valid @RequestBody WithdrawMemberRequest request,
            @RequestHeader("X-Member-Id") Long memberId  // TODO: JWT에서 추출하도록 변경
    ) {
        withdrawMemberService.withdrawMember(request.toServiceRequest(memberId));
    }
}
